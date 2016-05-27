/**
 * @author Robin Duda
 *
 * Handles the loading of game resources as a zip file.
 */


var patcher = {
    patch: {},
    index: 0,

    check: function (worker) {
        this.worker = worker;
        this.resource = worker.resource;

        $.ajax({
                type: "GET",
                url: this.resource + "/patch.json",
                contentType: "text/plain",

                success: (function (patch) {
                    patch.size = this.patchSize(patch);
                    this.patch = patch;
                    worker.completed(patch);
                }).bind(this),

                error: function () {
                    application.error("Failed to retrieve patch data.");
                }
            }
        );
    },

    update: function (worker) {
        var patch = patcher.patch;
        patcher.worker = worker;
        worker.started(patch.name, patch.build, patch.size, patch.files);
        patcher.download(0);
    },

    patchSize: function (patch) {
        var size = 0;

        for (var i = 0; i < patch.files.length; i++) {
            size += patch.files[i].size;
        }

        return size;
    },

    download: function (index) {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', this.resource + this.patch.files[index].path, true);
        xhr.responseType = 'arraybuffer';

        this.downloaded = 0;
        xhr.onload = this.completeHandler;
        xhr.addEventListener('progress', this.progressHandler);
        xhr.addEventListener('error', this.errorHandler);
        xhr.send();
    },

    delta: performance.now(),
    transferred: 0,
    downloaded: 0,

    progressHandler: function (event) {
        var bandwidth = (1000 * event.loaded) / (performance.now() - patcher.delta);

        patcher.transferred += (event.loaded - patcher.downloaded);
        patcher.downloaded = event.loaded;

        patcher.worker.progress(
            parseFloat(bandwidth).toFixed(2),
            patcher.transferred,
            patcher.downloaded,
            patcher.index
        );

        patcher.delta = performance.now();
    },

    completeHandler: function (e) {
        patcher.index += 1;

        if (patcher.index < patcher.patch.files.length)
            patcher.download(patcher.index);
        else
            patcher.worker.completed();
    },

    errorHandler: function () {
        application.error("Failed to retrieve file.");
    }
};