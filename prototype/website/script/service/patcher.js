/**
 * @author Robin Duda
 *
 * Handles the loading of game resources as a zip file.
 */
class Patcher {

    constructor() {
        this.resources = {};
    }

    check(callback) {
        this.callback = callback;
        this.resource = callback.resource;
        this.reset();
    }

    reset() {
        this.patch = {};
        this.index = 0;
        this.transferred = 0;
        this.downloaded = 0;
        this.chunks = 0;
        this.delta = performance.now() - 1000;

        this.network = new Network('patching.node');

        this.network.rest({
            accepted: (patch) => {
                patch.size = this.patchSize(patch);
                this.patch = patch;
                this.callback.completed();
            },
            error: () => {
                application.error("Failed to retrieve patch data.");
            }
        }, 'patchdata');
    }

    update(worker) {
        const patch = this.patch;
        this.worker = worker;
        worker.started(patch.name, patch.version, patch.size, patch.files);

        if (patch.files.length > 0) {
            this.download(0);
        } else {
            this.worker.completed();
        }
    }

    patchSize(patch) {
        let size = 0;

        for (let i = 0; i < patch.files.length; i++) {
            size += patch.files[i].size;
        }
        return size;
    }

    download(index) {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', this.resource + this.patch.files[index].path + "&version=" + this.patch.version, true);
        xhr.responseType = 'arraybuffer';

        this.downloaded = 0;
        xhr.onload = this.completeHandler;
        xhr.addEventListener('progress', this.progressHandler);
        xhr.onreadystatechange = this.errorHandler;
        xhr.send();
    }

    progressHandler(event) {
        this.chunks += (event.loaded - this.downloaded);
        this.transferred += (event.loaded - this.downloaded);
        this.downloaded = event.loaded;

        if ((performance.now() - this.delta) >= 1000) {
            this.bandwidth = this.chunks * 1000;
            this.delta = performance.now();
            this.chunks = 0;
        }

        this.worker.progress(
            parseFloat(this.bandwidth).toFixed(2),
            this.transferred,
            this.downloaded,
            this.index
        );
    }

    completeHandler(event) {
        if (event.target.status === 200) {
            this.resources[this.patch.files[this.index].path] = event.target.response;

            this.index += 1;

            if (this.index < this.patch.files.length) {
                this.download(this.index);
            } else {
                console.log(resources);
                this.worker.completed();
            }
        }
    }

    errorHandler(event) {
        if (event.target.status === 409) {
            this.reset();
        } else if (event.target.status === 404) {
            application.error("Failed to retrieve file.");
        }
    }
}

var patcher = new Patcher();