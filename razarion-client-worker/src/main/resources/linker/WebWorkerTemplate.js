importScripts('/NativeRazarion.js?t=' + (new Date).getTime());
self.$wnd = self;
self.$doc = self;
$wnd = self;
$wnd.window = self;
$wnd.window.console = self.console;
$wnd.console = self.console;
self.$wnd = self;
self.$wnd.console = self.console;
self.$wnd.window = self;
self.$wnd.window.console = self.console;
$stats = function () {
}
;
$sessionId = '';
var $strongName = '';
base = '/razarion-server/razarion_client_worker/';

function __MODULE_FUNC__() {
    var strongName;
    var answers = [];

    function unflattenKeylistIntoAnswers(propValArray, value_0) {
        var answer = answers;
        for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
            answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
        }
        answer[propValArray[n]] = value_0;
    }

    var values = [];
    values['user.agent'] = {'gecko1_8': 0, 'safari': 1};
    var providers = [];
    providers['user.agent'] = function () {
        var ua = navigator.userAgent.toLowerCase();
        if (ua.indexOf('webkit') > -1) {
            return 'safari';
        }
        if (ua.indexOf('gecko') > -1) {
            return 'gecko1_8';
        }
        throw new Error('Can not handle user agent ' + navigator.userAgent.toLowerCase());
    }

    function computePropValue(propName) {
        var value_0 = providers[propName](), allowedValuesMap = values[propName];
        if (value_0 in allowedValuesMap) {
            return value_0;
        }
        throw new Error('Property ' + propName + ' has no value ' + value_0);
    }

    try {
        // __PERMUTATIONS_BEGIN__
        // Permutation logic
        // __PERMUTATIONS_END__
    } catch (e) {
        var errorMsg = {
            "worker bootstrap error": e.message + " " + e.lineNumber
        };
        self.postMessage(JSON.stringify(errorMsg));
        return;
    }
    $strongName = strongName;
    importScripts(strongName + ".cache.js");
    gwtOnLoad(undefined, '__MODULE_NAME__', base);
}

__MODULE_FUNC__();
