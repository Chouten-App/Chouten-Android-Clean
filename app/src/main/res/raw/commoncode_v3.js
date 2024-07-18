var defaultSource = (function() {
    if (typeof source == "function") return new (source().default);
    return new (source.default);
})();

var console = {};
console.log = console.error = console.info = function (log) {
    Native.log(log)
};

function request(method, url, headers, body) {
    let res = Native.request(method, url, headers, body);
    return JSON.parse(res)
}