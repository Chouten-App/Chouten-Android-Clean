var defaultSource = new (source.default);

var console = {};
console.log = function (log) {
    Native.log(log)
};

function request(method, url, headers, body) {
    let res = Native.request(method, url, headers, body);
    return JSON.parse(res)
}