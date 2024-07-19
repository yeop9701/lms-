// viewer.js

// 전역 변수 선언
let player, userId, videoId, savedWatchingTime, lastUpdateTime = 0;

// 유틸리티 함수
function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
}

function sortNotes() {
    const notesList = $('#notesContainer');
    const notes = notesList.children('li').get();
    notes.sort((a, b) => {
        return parseInt($(a).find('.VideoTime').attr('data-time')) - 
                parseInt($(b).find('.VideoTime').attr('data-time'));
    });
    $.each(notes, (index, item) => notesList.append(item));
}

// YouTube 관련 함수
function onYouTubeIframeAPIReady() {
    const playerElement = document.getElementById('player');
    const videoUrl = playerElement.dataset.url;
    const playerTimeElement = document.getElementById('playerTime');
    const videoTime = parseInt(playerTimeElement.dataset.time, 10);

    player = new YT.Player('player', {
        height: '100%',
        width: '100%',
        videoId: videoUrl,
        playerVars: {
            'autoplay': 1,
            'start': videoTime
        },
        events: {
            'onStateChange': onPlayerStateChange
        }
    });

    window.player = player;
}

function onPlayerStateChange(event) {
    if (event.data == YT.PlayerState.ENDED) {
        saveUserVideo();
    } else if (event.data == YT.PlayerState.PLAYING) {
        setInterval(updateWatchingTime, 5000);
    }
}

function updateWatchingTime() {
    const currentTime = player.getCurrentTime();
    if (currentTime - lastUpdateTime >= 5) {
        saveUserVideo();
        lastUpdateTime = currentTime;
    }
}

function saveUserVideo() {
    const videoLength = player ? player.getDuration() : 0;
    const watchingTime = player ? player.getCurrentTime() : 0;
    const watchedPercentage = (watchingTime / videoLength) * 100;

    const videoData = {
        userId: userId,
        videoId: videoId,
        watched: watchedPercentage >= 80,
        watchedAt: new Date().toISOString(),
        watchingTime: watchingTime
    };

    $.ajax({
        type: "POST",
        url: "/userVideo/save",
        data: JSON.stringify(videoData),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (response) {
            console.log("User video saved successfully", response);
        },
        error: function (xhr, status, error) {
            console.error("Error saving user video", error);
        }
    });
}

// 노트 관련 함수
function createNoteElement(content, videoTime, noteId) {
    return $('<li>').addClass('d-flex').html(`
        <div class="d-flex flex-column">
            <span>${content}</span>
            <div>
                <i class="fa-solid fa-play"></i>
                <span class="VideoTime" data-time="${videoTime}">
                    ${formatTime(videoTime)}
                </span>
            </div>
        </div>
        <div class="d-flex align-items-center">
            <button class="btn btn-danger btn-circle btn-sm delete-note" data-note-id="${noteId}">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `);
}

function saveNoteToServer(videoId, content, videoTime, noteElement, editor) {
    $.ajax({
        url: `/note/create/${videoId}`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ content: content, videoTime: videoTime }),
        success: function(newNote) {
            // 새로운 노트 요소 생성
            const newNoteElement = createNoteElement(newNote.content, newNote.videoTime, newNote.id);
            // 기존의 임시 요소 대체
            noteElement.replaceWith(newNoteElement);
            editor.setMarkdown('');
            sortNotes();
        },
        error: function(xhr, status, error) {
            noteElement.find('span:first').text('마 영상 재생해라');
        }
    });
}

function updateNoteElement(noteElement, note) {
    noteElement.find('span:first').text(note.content);
    const formattedTime = formatTime(note.videoTime);
    noteElement.find('.VideoTime')
        .text(formattedTime)
        .attr('data-time', note.videoTime);
    noteElement.attr('data-note-id', note.id);
}

// 페이지 로드 시 실행
$(document).ready(function() {
    let token = $("meta[name='_csrf']").attr("content");
    let header = $("meta[name='_csrf_header']").attr("content");

    userId = $('#userId').val();
    videoId = $('#videoId').val();
    savedWatchingTime = $('#watchingTime').val();

    // sortNotes();

    // 이벤트 리스너 설정
    $('.lec_list > li > a').click(function(e) {
        e.preventDefault();
        const videoId = $(this).attr('href').split('/').pop();
        const savedWatchingTime = $(this).data('watching-time');
        window.location.href = `/video/viewer/${videoId}?time=${savedWatchingTime}`;
    });

    $('.VideoTime').each(function() {
        const videoTime = parseInt($(this).text(), 10);
        $(this).text(formatTime(videoTime));
        $(this).attr('data-time', videoTime);
    });

    $(document).on('click', '#notesContainer li', function(e) {
        const videoTime = parseInt($(this).find('.VideoTime').attr('data-time'), 10);
        if (typeof player !== 'undefined' && player !== null && typeof player.seekTo === 'function') {
            player.seekTo(videoTime, true);
        } else {
            console.log('Player is not ready or seekTo is not available');
        }
    });

    // 강의 영상 사이드 메뉴
    let tab_area = $('.tab_area > li');
    let tabAbtn = $('.side_menu > div > button');

    tabAbtn.each(function (index, item) {
        $(this).click(function () {                   
            tab_area.hide();
            tab_area.eq(index).show();
            
            tabAbtn.removeClass('on');
            tabAbtn.eq(index).addClass('on');
        });
    });

    // 디바운스 함수
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // 저장 중 플래그
    let isSaving = false;

    // 저장 함수
    // function saveNote() {
    //     if (isSaving) return; // 이미 저장 중이면 무시
    //     isSaving = true;

    //     const content = editor.getMarkdown();
    //     let videoTime = player ? player.getCurrentTime() : 0;
    //     const tempNoteElement = createNoteElement('저장 중...', videoTime, '');
    //     $('#notesContainer').append(tempNoteElement);
        
    //     saveNoteToServer(videoId, content, videoTime, tempNoteElement, editor)
    //         .then(() => {
    //             isSaving = false; // 저장 완료 후 플래그 해제
    //         })
    //         .catch((error) => {
    //             console.error('저장 중 오류 발생:', error);
    //             isSaving = false; // 오류 발생 시에도 플래그 해제
    //         });
    // }

    // saveNote 함수 수정
    function saveNote() {
        if (isSaving) return; // 이미 저장 중이면 무시
        isSaving = true;

        const content = editor.getMarkdown();
        if (content.trim() === '') {
            isSaving = false;
            return; // 내용이 비어있으면 저장하지 않음
        }

        let videoTime = player ? player.getCurrentTime() : 0;
        const tempNoteElement = createNoteElement('저장 중...', videoTime, '');
        $('#notesContainer').append(tempNoteElement);
        
        saveNoteToServer(videoId, content, videoTime, tempNoteElement, editor)
            .then(() => {
                isSaving = false; // 저장 완료 후 플래그 해제
            })
            .catch((error) => {
                console.error('저장 중 오류 발생:', error);
                isSaving = false; // 오류 발생 시에도 플래그 해제
            });
    }

    // 디바운스된 저장 함수
    const debouncedSave = debounce(saveNote, 300); // 300ms 대기

    // Toast UI Editor 초기화
    const editor = new toastui.Editor({
        el: document.querySelector('#editor'),
        height: 'calc(100% - 30px)',
        initialEditType: 'markdown',
        previewStyle: 'tab'
    });

    // Editor 요소에 키다운 이벤트 리스너 추가
    const editorElement = editor.getEditorElements().mdEditor;
    editorElement.addEventListener('keydown', function(event) {
        if (event.key === 'Enter' && !event.shiftKey && !event.ctrlKey && !event.altKey) {
            event.preventDefault();
            event.stopPropagation(); // 이벤트 전파 중지
            debouncedSave(); // 디바운스된 저장 함수 호출
        }
    });

    // 기존의 저장 버튼 클릭 이벤트 수정
    $(document).on('click', '#saveButton', function(event) {
        event.preventDefault();
        debouncedSave();
    });

    // saveNoteToServer 함수를 Promise를 반환하도록 수정
    // function saveNoteToServer(videoId, content, videoTime, noteElement, editor) {
    //     return new Promise((resolve, reject) => {
    //         $.ajax({
    //             url: `/note/create/${videoId}`,
    //             method: 'POST',
    //             contentType: 'application/json',
    //             data: JSON.stringify({ content: content, videoTime: videoTime }),
    //             success: function(newNote) {
    //                 const newNoteElement = createNoteElement(newNote.content, newNote.videoTime, newNote.id);
    //                 noteElement.replaceWith(newNoteElement);
    //                 editor.setMarkdown('');
    //                 sortNotes();
    //                 resolve();
    //             },
    //             error: function(xhr, status, error) {
    //                 noteElement.find('span:first').text('저장 실패');
    //                 reject(error);
    //             }
    //         });
    //     });
    // }

    function saveNoteToServer(videoId, content, videoTime, noteElement, editor) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: `/note/create/${videoId}`,
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ content: content, videoTime: videoTime }),
                success: function(newNote) {
                    const newNoteElement = createNoteElement(newNote.content, newNote.videoTime, newNote.id);
                    noteElement.replaceWith(newNoteElement);
                    editor.setMarkdown('');
                    sortNotes();
                    resolve();
                },
                error: function(xhr, status, error) {
                    noteElement.remove(); // 저장 실패 시 임시 요소 제거
                    reject(error);
                }
            });
        });
    }

    // 노트 저장 버튼 클릭 이벤트
    $(document).on('click', '#saveButton', function() {
        const content = editor.getMarkdown();
        let videoTime = player ? player.getCurrentTime() : 0;
        const tempNoteElement = createNoteElement('저장 중...', videoTime, '');
        $('#notesContainer').append(tempNoteElement);
        saveNoteToServer(videoId, content, videoTime, tempNoteElement, editor);
    });

    // 노트 삭제 버튼 클릭 이벤트
    $(document).on('click', '.delete-note', function() {
        const noteId = $(this).data('note-id');
        const noteElement = $(this).closest('li');

        if (confirm('정말로 이 노트를 삭제하시겠습니까?')) {
            $.ajax({
                url: `/note/delete/${noteId}`,
                method: 'POST',
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(header, token);
                },
                success: function() {
                    noteElement.remove();
                    console.log('노트가 성공적으로 삭제되었습니다.');
                    alert('노트가 성공적으로 삭제되었습니다.');
                    let url = new URL(window.location.href);
                    url.searchParams.delete('n');
                    window.history.replaceState({}, '', url);
                },
                error: function(xhr, status, error) {
                    console.error('노트 삭제 중 오류 발생:', error);
                    alert('노트 삭제 중 오류가 발생했습니다.');
                }
            });
        }
    });

    // 페이지 언로드 시 비디오 정보 저장
    window.addEventListener('beforeunload', function(event) {
        saveUserVideo();
    });

    // YouTube API 초기화
    onYouTubeIframeAPIReady();
});