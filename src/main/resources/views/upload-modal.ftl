<div class="modal fade" id="upload-modal" tabindex="-1" aria-labelledby="upload-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="upload-modal-label">Sound Upload</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <ul class="nav nav-tabs" id="uploadTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="file-upload-tab" data-bs-toggle="tab" data-bs-target="#file-upload" type="button" role="tab" aria-controls="file-upload" aria-selected="true">File Upload</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="youtube-upload-tab" data-bs-toggle="tab" data-bs-target="#youtube-upload" type="button" role="tab" aria-controls="youtube-upload" aria-selected="false">YouTube Upload</button>
                    </li>
                </ul>
                <div class="tab-content pt-2" id="uploadTabContent">
                    <div class="tab-pane fade show active" id="file-upload" role="tabpanel" aria-labelledby="file-upload-tab">
                        <form id="upload-form" novalidate>
                            <div class="mb-3">
                                <label for="name-input" class="form-label">Name</label>
                                <input type="text" class="form-control" id="name-input" placeholder="Enter name" required>
                                <div class="invalid-feedback">
                                    Please enter a name.
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="tags-input" class="form-label">Tags</label>
                                <input type="text" class="form-control" id="tags-input" placeholder="Enter tags, separated by commas"
                                       pattern="^(\s*[^,]+\s*)(,\s*[^,]+\s*){0,2}$">
                                <div class="invalid-feedback">
                                    Please enter up to 3 tags, separated by commas.
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="file-input" class="form-label">File upload</label>
                                <input class="form-control" type="file" id="file-input" required>
                                <div class="invalid-feedback">
                                    Please upload a file.
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="tab-pane fade" id="youtube-upload" role="tabpanel" aria-labelledby="youtube-upload-tab">
                        <form id="youtube-form" novalidate>
                            <div class="mb-3">
                                <label for="youtube-name-input" class="form-label">Name</label>
                                <input type="text" class="form-control" id="youtube-name-input" placeholder="Enter name" required>
                                <div class="invalid-feedback">
                                    Please enter a name.
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="youtube-tags-input" class="form-label">Tags</label>
                                <input type="text" class="form-control" id="youtube-tags-input" placeholder="Enter tags, separated by commas"
                                       pattern="^(\s*[^,]+\s*)(,\s*[^,]+\s*){0,2}$">
                                <div class="invalid-feedback">
                                    Please enter up to 3 tags, separated by commas.
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="youtube-link-input" class="form-label">YouTube Link</label>
                                <input class="form-control" type="url" id="youtube-link-input" placeholder="Enter YouTube link"
                                       pattern=".*(youtube\.com|youtu\.be).*" required>
                                <div class="invalid-feedback">
                                    Please enter a valid YouTube link.
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" id="upload-file-button" onclick="uploadSound()">Upload File</button>
                <button type="button" class="btn btn-primary" id="upload-youtube-button" onclick="uploadVideo()" style="display: none;">Upload YouTube</button>
                <div id="upload-spinner" class="spinner-border text-primary" role="status" style="display: none;">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.getElementById('file-upload-tab').addEventListener('click', function () {
        document.getElementById('upload-file-button').style.display = 'inline-block';
        document.getElementById('upload-youtube-button').style.display = 'none';
    });

    document.getElementById('youtube-upload-tab').addEventListener('click', function () {
        document.getElementById('upload-file-button').style.display = 'none';
        document.getElementById('upload-youtube-button').style.display = 'inline-block';
    });
</script>