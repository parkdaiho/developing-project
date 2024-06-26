const area = document.getElementById("my-page-area");

function getMemberInfoPage() {
    let url = "/my-page/information";

    getPageInMyPage(url, "MEMBER-INFO");
}

function getWithdrawalPage(userid) {
    let url = "/my-page/withdrawal/" + userid;

    getPageInMyPage(url, "WITHDRAWAL");
}

function getMembershipPage(page, sort, searchSort, query) {
    let params = new URLSearchParams({
        "page": page,
        "sort": sort,
        "searchSort": searchSort,
        "query": query
    });
    let url = "/my-page/membership?" + params;

    getPageInMyPage(url, "MEMBERSHIP");
}

function getPageInMyPage(url, pageName) {
    fetch(url, {
        method: Method.GET,
        headers: getHeaders(false),
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                alert("Loading " + pageName + " page failed.");
            }
        })
        .then(result => {
            area.innerHTML = result;
        })
}