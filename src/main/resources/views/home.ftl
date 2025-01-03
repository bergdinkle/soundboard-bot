<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Soundboard Bot</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="/static/css/main.css" rel="stylesheet">
    <link href="/static/css/mobile.css" rel="stylesheet">
</head>
<body>
<div class="head-container">
    <h4>Soundboard Bot</h4>
    <div class="right-container">
        <#if status.connected == true>
            <div class="status-text status-text-connected form-control">Currently connected to "${status.currentChannel}
                "
            </div>
        <#else>
            <div class="status-text status-text-disconnected form-control">Not connected</div>
        </#if>

        <input style="display: inline" class="form-control" id="search-input" placeholder="Search">
        <button type="button" class="btn btn-primary head-button" data-bs-toggle="modal" data-bs-target="#upload-modal">
            Upload
        </button>
    </div>
</div>
<#include "upload-modal.ftl">
<div class="list-group">
    <#list sounds as sound>
        <div class="list-group-item list-group-item-action">
            <button type="button" class="custom-button play-button" onclick="playSound('${sound.id}')">
                <img src="/static/svg/play.svg">
                ${sound.name}
                <div class="tags-container">
                    <#list sound.tags as tag>
                        <span class="badge rounded-pill text-bg-primary">${tag}</span>
                    </#list>
                </div>
            </button>
            <div class="dropdown-center more-button">
                <button id="more-button-${sound.id}" type="button" class="custom-button" data-bs-toggle="dropdown"
                        data-bs-auto-close="outside" aria-expanded="false">
                    <img src="/static/svg/three-dots-vertical.svg">
                </button>
                <ul class="dropdown-menu">
                    <li><span class="dropdown-item-text">Created by ${sound.submittedByName}</span></li>
                    <li>
                        <hr class="dropdown-divider">
                    </li>
                    <li>
                        <button class="dropdown-item more-menu-button" type="button"
                                onclick="playSoundLocal('${sound.id}')"><img src="/static/svg/play.svg">Play local
                        </button>
                    </li>
                    <li><a href="/api/sound/${sound.id}/download" download="" class="dropdown-item more-menu-button"
                           onclick="hideDropdown('${sound.id}')"><img src="/static/svg/download.svg">Download</a></li>
                    <li>
                        <button class="dropdown-item more-menu-button<#if sound.submittedById != user.id && !user.admin> disabled</#if>"<#if sound.submittedById != user.id && !user.admin> aria-disabled="true"</#if>
                                type="button" onclick="deleteSound('${sound.id}')"><img src="/static/svg/delete.svg">Delete
                        </button>
                    </li>
                </ul>

            </div>
            <button id="favorite-button-${sound.id}" type="button" class="custom-button favorite-button"
                    onclick="favorite('${sound.id}')">
                <img <#if sound.favorite>style="display: none"</#if> src="/static/svg/not-favorite.svg"/>
                <img <#if !sound.favorite>style="display: none"</#if> src="/static/svg/favorite.svg"/>
            </button>
        </div>
    </#list>
</div>
<script src="/static/js/main.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
</body>
</html>