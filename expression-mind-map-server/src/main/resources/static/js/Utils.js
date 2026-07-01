/**
 *
 * 工具类使用
 * @author liukx
 * @date  -
 */
let engineUtils = function () {

    /**
     * 统一处理 ajax 401（未登录/登录过期）：跳转登录页并携带当前页地址，便于登录后回跳。
     * 同一次会话内只跳一次，避免并发请求重复跳转。
     */
    let redirectingToLogin = false;
    function redirectToLogin() {
        if (redirectingToLogin) {
            return;
        }
        redirectingToLogin = true;
        let current = location.pathname + location.search + location.hash;
        location.href = '/template/login.html?redirect=' + encodeURIComponent(current);
    }

    let requestPost = function post(url, data, callback) {
        let $ = layui.$;
        $.ajax({
            url: url, // 替换为你的API端点URL
            type: 'POST',
            contentType: 'application/json', // 指定发送的数据格式为JSON
            data: JSON.stringify(data), // 需要发送的JSON数据
            dataType: 'json', // 指定预期从服务器返回的数据类型
            success: function (response) {
                if (response.code === 200) {
                    callback(response);
                } else {
                    layer.alert(response.message, {
                        title: '错误提示'
                    });
                }
            },
            error: function (xhr, status, error) {
                // 登录过期统一跳转登录页
                if (xhr.status === 401) {
                    redirectToLogin();
                    return;
                }
                // 请求失败时的回调函数
                console.error(error);
            }
        });
    };

    /**
     * 静默 POST 请求 - 不弹出错误提示，始终执行回调
     * 适用于自动补全等场景
     *
     * @param url 请求地址
     * @param data 请求数据
     * @param callback 回调函数，无论成功失败都会执行，参数为 response 或 null
     */
    let requestPostSilent = function postSilent(url, data, callback) {
        let $ = layui.$;
        $.ajax({
            url: url,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            dataType: 'json',
            success: function (response) {
                callback(response);
            },
            error: function (xhr, status, error) {
                // 登录过期统一跳转登录页，否则静默回调 null
                if (xhr.status === 401) {
                    redirectToLogin();
                    return;
                }
                // 请求失败时传递 null 给回调
                callback(null);
            }
        });
    };

    /**
     * 静默 GET 请求 - 不弹出错误提示，始终执行回调
     * 适用于自动补全等场景
     *
     * @param url 请求地址（参数已包含在 url 中）
     * @param callback 回调函数，无论成功失败都会执行，参数为 response 或 null
     */
    let requestGetSilent = function getSilent(url, callback) {
        let $ = layui.$;
        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                callback(response);
            },
            error: function (xhr, status, error) {
                // 登录过期统一跳转登录页，否则静默回调 null
                if (xhr.status === 401) {
                    redirectToLogin();
                    return;
                }
                // 请求失败时传递 null 给回调
                callback(null);
            }
        });
    };
    let getRequestParams = function getRequest() {
        var url = location.search; //获取url中"?"符后的字串
        var theRequest = new Object();
        if (url.indexOf("?") !== -1) {
            var str = url.substring(1);
            strs = str.split("&");
            for (var i = 0; i < strs.length; i++) {
                let key = strs[i].split("=")[0];
                if (key !== "") {
                    theRequest[key] = decodeURIComponent(strs[i].split("=")[1]);
                }
            }
        }
        return theRequest;
    };

    /**
     * 比较两个日期字符串的大小
     *
     * @param {string} dateStr1 - 第一个日期字符串，格式为 "YYYY-MM-DD"
     * @param {string} dateStr2 - 第二个日期字符串，格式为 "YYYY-MM-DD"
     * @returns {number} - 返回值为 -1, 0, 1 分别表示 dateStr1 小于、等于、大于 dateStr2
     */
    let compareDateStr = function compareDateStr(dateStr1, dateStr2) {
        // 将日期字符串转换为 Date 对象
        const date1 = new Date(dateStr1);
        const date2 = new Date(dateStr2);

        // 检查日期字符串是否有效
        if (isNaN(date1.getTime()) || isNaN(date2.getTime())) {
            throw new Error('Invalid date string');
        }

        // 比较两个日期
        if (date1 < date2) {
            return -1;
        } else if (date1 > date2) {
            return 1;
        } else {
            return 0;
        }
    }
    return {
        requestPost: requestPost,
        requestPostSilent: requestPostSilent,
        requestGetSilent: requestGetSilent,
        getRequestParams: getRequestParams,
        compareDateStr: compareDateStr
    }
}()
