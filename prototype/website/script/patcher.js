/**
 * @author Robin Duda
 *
 * Handles the loading of game resources as a zip file.
 */

var resources = {};

var patcher = {
    check: function (checker) {
        this.checker = checker;
        this.resource = checker.resource;
        this.reset();
    },

    reset: function () {
        patcher.patch = {};
        patcher.index = 0;
        patcher.transferred = 0;
        patcher.downloaded = 0;
        patcher.chunks = 0;
        patcher.delta = performance.now() - 1000;

        $.ajax({
                type: "GET",
                url: patcher.resource + "/api/patchdata",
                contentType: "text/plain",
                dataType: "json",
                success: (function (patch) {
                    patch.size = patcher.patchSize(patch);
                    patcher.patch = patch;
                    patcher.checker.completed();
                }),

                error: function () {
                    application.error("Failed to retrieve patch data.");
                }
            }
        );
    },

    update: function (worker) {
        var patch = patcher.patch;
        patcher.worker = worker;
        worker.started(patch.name, patch.version, patch.size, patch.files);
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
        xhr.open('GET', this.resource + "/api/download?file=" + patcher.patch.files[index].path + "&version=" + patcher.patch.version, true);
        xhr.responseType = 'arraybuffer';

        patcher.downloaded = 0;
        xhr.onload = patcher.completeHandler;
        xhr.addEventListener('progress', patcher.progressHandler);
        xhr.onreadystatechange = patcher.errorHandler;
        xhr.send();
    },

    progressHandler: function (event) {
        patcher.chunks += (event.loaded - patcher.downloaded);
        patcher.transferred += (event.loaded - patcher.downloaded);
        patcher.downloaded = event.loaded;

        if ((performance.now() - patcher.delta) >= 1000) {
            patcher.bandwidth = patcher.chunks * 1000;
            patcher.delta = performance.now();
            patcher.chunks = 0;
        }

        patcher.worker.progress(
            parseFloat(patcher.bandwidth).toFixed(2),
            patcher.transferred,
            patcher.downloaded,
            patcher.index
        );
    },

    completeHandler: function (event) {
        if (event.target.status == 200) {
            resources[patcher.patch.files[patcher.index].path] = event.target.response;
            
            patcher.index += 1;

            if (patcher.index < patcher.patch.files.length) {
                patcher.download(patcher.index);
            } else {
                console.log(resources);
                patcher.worker.completed();
            }
        }
    },

    errorHandler: function (event) {
        if (event.target.status == 409) {
            patcher.reset();
        } else if (event.target.status == 404) {
            application.error("Failed to retrieve file.");
        }
    }
};