function playSoundLocal(id) {
    let audio = new Audio(`/api/sound/${id}/download`);
    audio.play().then(() => hideDropdown(id)).catch(err => hideDropdown(id))

}

function deleteSound(id) {
    let token = getCookie("Auth-Token")
    let url = `/api/sound/${id}`
    sendRequest(url, "DELETE", token, null, null, () => location.reload())
}

function hideDropdown(soundId) {
    let toggleElement = document.getElementById(`more-button-${soundId}`)
    let dropdown = new bootstrap.Dropdown(toggleElement)
    dropdown.hide()
}

function uploadSound() {
    var modal = bootstrap.Modal.getInstance(document.getElementById('upload-modal'));

    let nameInput = document.getElementById("name-input")
    let tagsInput = document.getElementById("tags-input")
    let fileInput = document.getElementById("file-input")
    let form = document.getElementById('upload-form');

    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    let uploadButton = document.getElementById("upload-file-button")
    uploadButton.disabled = true
    let spinner = document.getElementById("upload-spinner")
    spinner.style.display = "block"

    let name = nameInput.value
    let tags = []
    if (tagsInput.value) {
        tags = tagsInput.value.split(",").map(tag => tag.trim())
    }

    const file = fileInput.files[0];
    const reader = new FileReader();

    let onSuccess = () => {
        modal.hide()
        form.classList.remove('was-validated');
        nameInput.value = ""
        tagsInput.value = ""
        fileInput.value = ""
        spinner.style.display = "none"
        uploadButton.disabled = false
        location.reload()
    }

    let onError = () => {
        form.classList.remove('was-validated');
        spinner.style.display = "none"
        uploadButton.disabled = false
    }

    reader.addEventListener("load", () => {
        let fileEncoded = reader.result.split(",")[1]
        let data = {
            "name": name,
            "data": fileEncoded,
            "tags": tags
        }
        let token = getCookie("Auth-Token")
        sendRequest("/api/sound", "POST", token, JSON.stringify(data), 'application/json', onSuccess, onError)
    }, false);

    if (file) {
        reader.readAsDataURL(file);
    }
}

function uploadVideo() {
    var modal = bootstrap.Modal.getInstance(document.getElementById('upload-modal'));

    let nameInput = document.getElementById("youtube-name-input")
    let tagsInput = document.getElementById("youtube-tags-input")
    let linkInput = document.getElementById("youtube-link-input")
    let form = document.getElementById('youtube-form');

    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    let uploadButton = document.getElementById("upload-youtube-button")
    uploadButton.disabled = true
    let spinner = document.getElementById("upload-spinner")
    spinner.style.display = "block"

    let name = nameInput.value
    let tags = []
    if (tagsInput.value) {
        tags = tagsInput.value.split(",").map(tag => tag.trim())
    }
    let link = linkInput.value

    let onSuccess = () => {
        modal.hide()
        form.classList.remove('was-validated');
        nameInput.value = ""
        tagsInput.value = ""
        linkInput.value = ""
        spinner.style.display = "none"
        uploadButton.disabled = false
        location.reload()
    }

    let onError = () => {
        form.classList.remove('was-validated');
        spinner.style.display = "none"
        uploadButton.disabled = false
    }

    let data = {
        "name": name,
        "link": link,
        "tags": tags
    }
    let token = getCookie("Auth-Token")
    sendRequest("/api/sound/youtube", "POST", token, JSON.stringify(data), 'application/json', onSuccess, onError)
}

let searchInput = document.getElementById("search-input")

searchInput.oninput = filter

function filter() {
    let value = searchInput.value.toLowerCase()

    let container = document.querySelector(".list-group")
    let elements = container.children
    let first = null
    let current = null
    for (let i = 0; i < elements.length; i++) {
        let currItem = elements[i];
        if(currItem.innerHTML.toLowerCase().includes(value)) {
            if(!first) {
                first = currItem
            }
            current = currItem
            currItem.style.display = ""
        } else {
            currItem.style.display = "none"
        }
    }
    let last = current

    checkWrap()
    if (first && last) {
        last.style.borderBottomLeftRadius = "inherit"
        last.style.borderBottomRightRadius = "inherit"
        first.style.borderTopWidth = "var(--bs-list-group-border-width)"
        first.style.borderTopLeftRadius = "inherit"
        first.style.borderTopRightRadius = "inherit"
    }
}

window.onload = checkWrap
window.onresize = checkWrap
onscroll = checkWrap


function checkWrap() {
    let container = document.querySelector(".list-group")
    let headline = document.querySelector("h4")
    container.style.top = headline.getBoundingClientRect().bottom.toString() + "px"

    let elements = container.children
    let prevItem;

    for (let i = 0; i < elements.length; i++) {
        let currItem = elements[i];

        if (currItem.style.display === "none") {
            continue
        }

        if (prevItem && prevItem.getBoundingClientRect().top > currItem.getBoundingClientRect().top) {
            prevItem.style.borderBottomLeftRadius = "inherit"
            prevItem.style.borderBottomRightRadius = "inherit"
            currItem.style.borderTopWidth = "var(--bs-list-group-border-width)"
            currItem.style.borderTopLeftRadius = "inherit"
            currItem.style.borderTopRightRadius = "inherit"
        } else if (i !== 0 && i !== elements.length - 1) {
            currItem.style.borderBottomLeftRadius = "0"
            currItem.style.borderBottomRightRadius = "0"
            currItem.style.borderTopWidth = "0"
            currItem.style.borderTopLeftRadius = "0"
            currItem.style.borderTopRightRadius = "0"
        } else if (i === 0) {
            currItem.style.borderBottomLeftRadius = "0"
            currItem.style.borderBottomRightRadius = "0"
        } else if (i === elements.length - 1) {
            currItem.style.borderTopWidth = "0"
            currItem.style.borderTopLeftRadius = "0"
            currItem.style.borderTopRightRadius = "0"
        }
        prevItem = currItem;
    }
}

function favorite(id) {
    let button = document.getElementById("favorite-button-" + id)
    let favorite = false

    if (button.children[0].style.display !== "none") {
        favorite = true
    }

    let token = getCookie("Auth-Token")
    let url = `/api/sound/${id}/favorite`
    sendRequest(url, "POST", token, JSON.stringify({"favorite": favorite}), 'application/json', () => location.reload())
}

function playSound(id) {
    let token = getCookie("Auth-Token")
    let url = `/api/sound/${id}/play`
    sendRequest(url, "POST", token)
}

function sendRequest(url, method, token, body, contentType, onSuccess, onError) {
    let xhr = new XMLHttpRequest();
    xhr.open(method, url, true);
    xhr.setRequestHeader("Auth-Token", token);
    if (contentType) {
        xhr.setRequestHeader('Content-type', contentType);
    }

    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (!xhr.status.toString().startsWith("2")) {
                if (onError) {
                    onError()
                }
            } else {
                if (onSuccess) {
                    onSuccess()
                }
            }
        }
    };
    xhr.send(body);
}

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for(let i = 0; i <ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}