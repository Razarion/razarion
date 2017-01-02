// Window and document redirection to WorkerGlobalScope
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
// Errai
erraiBusRemoteCommunicationEnabled = false;
erraiJaxRsJacksonMarshallingActive = true;
// GWT
$stats = function () {
};
$sessionId = "";
var $strongName = "";
base = '/razarion-server/razarion_client_worker/'; // Used for logging

function __MODULE_FUNC__() {
    var strongName;
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
