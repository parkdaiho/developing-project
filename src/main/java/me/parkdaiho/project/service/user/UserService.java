package me.parkdaiho.project.service.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkdaiho.project.config.PrincipalDetails;
import me.parkdaiho.project.config.oauth2.OAuth2AuthenticationCustomException;
import me.parkdaiho.project.config.oauth2.OAuth2UserInfo;
import me.parkdaiho.project.config.properties.CookieProperties;
import me.parkdaiho.project.config.properties.JwtProperties;
import me.parkdaiho.project.config.properties.PaginationProperties;
import me.parkdaiho.project.domain.Sort;
import me.parkdaiho.project.domain.user.Token;
import me.parkdaiho.project.domain.user.Role;
import me.parkdaiho.project.domain.user.User;
import me.parkdaiho.project.dto.user.SendCodeForPasswordRequest;
import me.parkdaiho.project.dto.user.SendCodeForUsernameRequest;
import me.parkdaiho.project.dto.user.MembershipSearchRequest;
import me.parkdaiho.project.dto.user.*;
import me.parkdaiho.project.repository.user.TokenRepository;
import me.parkdaiho.project.repository.user.UserRepository;
import me.parkdaiho.project.service.EmailService;
import me.parkdaiho.project.service.RedisService;
import me.parkdaiho.project.util.CookieUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    private final RedisService redisService;
    private final EmailService emailService;

    private final PaginationProperties paginationProperties;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    public SignUpResponse signUp(SignUpRequest dto) {
        tokenRepository.save(new Token(dto.toEntity()));

        return new SignUpResponse(dto.getUsername(), dto.getPassword());
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user_id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected username: " + username));
    }

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected nickname: " + nickname));
    }

    public Boolean isUser(PrincipalDetails principal) {
        User user = principal.getUser();
        Role role = user.getRole();

        return role.getIsUser();
    }

    public void addAttributesForMyPage(PrincipalDetails principal, Model model) {
        User user = principal.getUser();

        model.addAttribute("id", user.getId());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("email", user.getEmail());
    }

    public Page<MembershipViewResponse> getUsers(MembershipSearchRequest request, PrincipalDetails principal) {
        if(request.getPage() == null) request.setPage(1);
        if(request.getSort() == null || request.getSort().isBlank()) request.setSort(Sort.ALL.getProperty());

        if(principal.getRole().getIsUser()) throw new IllegalArgumentException("Non-Authority");

        Pageable pageable = PageRequest.of(request.getPage() - 1, paginationProperties.getUsersPerPage());
        Sort sort = Sort.valueOf(request.getSort().toUpperCase());
        String query = request.getQuery();
        if(query == null || query.isEmpty()) {
            return getUsersBySort(sort, pageable);
        }

        Sort searchSort = Sort.valueOf(request.getSearchSort().toUpperCase());
        switch (sort) {
            case ALL -> {
                return getAllUsersByQuery(query, searchSort, pageable);
            }
            case ADMIN, MANAGER, USER, WITHDRAWN -> {
                Role role = Role.valueOf(sort.getProperty());
                return getUsersByRoleAndQuery(role, query, searchSort, pageable);
            }

            default -> throw new IllegalArgumentException("Unexpected sort : " + sort);
        }
    }

    private Page<MembershipViewResponse> getUsersBySort(Sort sort, Pageable pageable) {
        Page<User> users;
        switch (sort) {
            case ALL -> users = userRepository.findAll(pageable);
            case ADMIN, MANAGER, USER, WITHDRAWN -> {
                Role role = Role.valueOf(sort.getProperty());
                users = userRepository.findByRole(role, pageable);
            }

            default -> throw new IllegalArgumentException("Unexpected sort: " + sort);
        }

        return users.map(entity -> new MembershipViewResponse(entity));
    }

    private Page<MembershipViewResponse> getUsersByRoleAndQuery(Role role, String query, Sort searchSort, Pageable pageable) {
        Page<User> users;
        switch (searchSort) {
            case USERNAME -> users = userRepository.findByRoleAndUsernameContaining(role, query, pageable);
            case NICKNAME -> users = userRepository.findByRoleAndNicknameContaining(role, query, pageable);

            default -> throw new IllegalArgumentException("Unexpected search-sort: " + searchSort);
        }

        return users.map(entity -> new MembershipViewResponse(entity));
    }

    private Page<MembershipViewResponse> getAllUsersByQuery(String query, Sort searchSort, Pageable pageable) {
        Page<User> users;
        switch (searchSort) {
            case USERNAME -> users = userRepository.findByUsernameContaining(query, pageable);
            case NICKNAME -> users = userRepository.findByNicknameContaining(query, pageable);

            default -> throw new IllegalArgumentException("Unexpected search-sort: " + searchSort);
        }

        return users.map(entity -> new MembershipViewResponse(entity));
    }

    public void addAttributesForMembership(Page<MembershipViewResponse> users, Model model) {
        paginationProperties.addPaginationAttributesToModel(users, model, paginationProperties.getUserPagesPerBlock());

        model.addAttribute("users", users.getContent());
    }

    public User getOAuth2UserByEmail(String email, OAuth2UserInfo oAuth2UserInfo) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationCustomException("error", oAuth2UserInfo));
    }

    @Transactional
    public void changeRole(ChangeRoleRequest request, PrincipalDetails principal) {
        if (!principal.getRole().equals(Role.ADMIN)) throw new IllegalArgumentException("No Authority");

        User user = findById(request.getId());
        user.changeRole(request.getNewRole());
    }

    public Boolean nicknameDupCheckInMyPage(NicknameDupCheckRequest request, PrincipalDetails principal) {
        if(principal.getUserId() != findByNickname(request.getOriginalNickname()).getId()) {
            throw new IllegalArgumentException("No-authority");
        }

        try {
            findByNickname(request.getNickname());

            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Transactional
    public void modifyUser(ModifyUserInfoRequest request, PrincipalDetails principal) {
        User user = findById(request.getUserId());
        checkAuthority(user, principal);

        user.update(request.getPassword(), request.getNickname());
    }

    @Transactional
    public void deleteUser(HttpServletRequest request, HttpServletResponse response,
                           WithdrawalMemberRequest dto, PrincipalDetails principal) {
        User user = findById(dto.getUserId());
        checkAuthority(user, principal);

        user.checkPassword(dto.getPassword())
                .makeWithdrawnMember();

        CookieUtils.deleteCookie(request, response, jwtProperties.getRefreshTokenCookieName());
    }

    private void checkAuthority(User user, PrincipalDetails principal) {
        if (principal.getUserId().equals(user.getId()) || principal.getRole() != Role.USER) return;

        throw new IllegalArgumentException("No Authority");
    }

    @Transactional
    public void deleteUser(WithdrawalMemberRequest request, PrincipalDetails principal) {
        User user = findById(request.getUserId());
        checkAuthority(user, principal);

        user.makeWithdrawnMember();
    }

    public Boolean usernameDupCheckInSignUp(UsernameDupCheckRequest request) {
        if(request.getUsername() == null || request.getUsername().isBlank()) return false;

        try {
            findByUsername(request.getUsername());

            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean nicknameDupCheckInSignUp(NicknameDupCheckRequest request) {
        if(request.getNickname() == null || request.getNickname().isBlank()) return false;

        try {
            findByNickname(request.getNickname());

            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean emailDupCheckInSignUp(EmailDupCheckRequest request) {
        if(request.getEmail() == null || request.getEmail().isBlank()) return false;

        try {
            findByEmail(request.getEmail());

            return false;
        } catch (Exception e) {
            sendAuthenticationNumber(request.getEmail());

            return true;
        }
    }

    @Transactional
    protected void sendAuthenticationNumber(String email) {
        String code = emailService.sendAuthenticationCodeForSignUp(email);

        redisService.putRedisTemplate(email, code);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user: " + email));
    }

    public String getSavedUsername(HttpServletRequest request) {
        Cookie cookie = CookieUtils.getCookieByName(request, cookieProperties.getSavedUsernameName());
        if(cookie == null) return null;

        String serializedUsername = cookie.getValue();

        return CookieUtils.deserialize(serializedUsername, String.class);
    }

    public Boolean emailAuthCheckInSignUp(EmailAuthCheckRequest request) {
        try {
            String savedCode = redisService.getValue(request.getEmail());
            String code = request.getCode();

            if(code == null || code.isBlank()) return false;

            return savedCode.equals(code);
        } catch (Exception e) {

            return false;
        }
    }

    @Transactional
    public void sendCode(SendCodeForUsernameRequest request) {
        String email = request.getEmail();
        if(email == null) throw new IllegalArgumentException("Email is null.");

        findByEmail(email);

        String code = emailService.sendAuthenticationCodeForUsername(email);
        redisService.putRedisTemplate(email, code);
    }

    @Transactional
    public void emailAuthCheckInFindUsername(EmailAuthCheckRequest request, HttpServletResponse response) {
        String email = request.getEmail();
        if(email == null) throw new IllegalArgumentException("Email is null.");

        String authNumber = request.getCode();
        if(authNumber == null) throw new IllegalArgumentException("AuthNumber is null.");

        boolean flag = authNumber.equals(redisService.getValue(email));

        if(flag) {
            String serializedUsername = CookieUtils.serialize(findByEmail(email).getUsername());
            CookieUtils.addCookie(response, cookieProperties.getUsernameInFindUsernameName(),
                    serializedUsername, cookieProperties.getFindUserInfoExpiry());
        } else {
            throw new IllegalArgumentException("The code is not the same.");
        }
    }

    @Transactional
    public String getFoundUsername(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = CookieUtils.getCookieByName(request, cookieProperties.getUsernameInFindUsernameName());
        if(cookie == null) throw new IllegalArgumentException("Unexpected access");

        String username = CookieUtils.deserialize(cookie.getValue(), String.class);

        CookieUtils.deleteCookie(request, response, cookieProperties.getUsernameInFindUsernameName());

        return username;
    }

    @Transactional
    public void sendCode(SendCodeForPasswordRequest request) {
        String username = request.getUsername();
        if(username == null) throw new IllegalArgumentException("Username is null.");

        String email = request.getEmail();
        if(email == null) throw new IllegalArgumentException("Email is null.");

        User user = findByUsername(username);
        if(!email.equals(user.getEmail())) throw new IllegalArgumentException("Check input.");

        String code = emailService.sendAuthenticationCodeForPassword(email);
        redisService.putRedisTemplate(email, code);
    }

    public void emailAuthCheckInFindPassword(EmailAuthCheckRequest request, HttpServletResponse response) {
        String email = request.getEmail();
        if(email == null) throw new IllegalArgumentException("Email is null.");

        String authNumber = request.getCode();
        if(authNumber == null) throw new IllegalArgumentException("AuthNumber is null.");

        boolean flag = authNumber.equals(redisService.getValue(email));
        if(flag) {
            String serializedEmail = CookieUtils.serialize(email);
            CookieUtils.addCookie(response, cookieProperties.getEmailInFindPasswordName(),
                    serializedEmail, cookieProperties.getFindUserInfoExpiry());
        } else {
            throw new IllegalArgumentException("Check code.");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest dto,
                               HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = CookieUtils.getCookieByName(request, cookieProperties.getEmailInFindPasswordName());
        if(cookie == null) throw new IllegalArgumentException("Unexpected access");

        String email = CookieUtils.deserialize(cookie.getValue(), String.class);
        User user = findByEmail(email);

        user.update(dto.getPassword());
        
        CookieUtils.deleteCookie(request, response, cookieProperties.getEmailInFindPasswordName());
    }

    public void checkCookieForPage(HttpServletRequest request, String cookieName) {
        Cookie cookie = CookieUtils.getCookieByName(request, cookieName);
        if(cookie == null) throw new IllegalArgumentException("Unexpected access");
    }

    public void deleteExistingCookieForFindUserInfo(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, cookieProperties.getUsernameInFindUsernameName());
        CookieUtils.deleteCookie(request, response, cookieProperties.getEmailInFindPasswordName());
    }
}
