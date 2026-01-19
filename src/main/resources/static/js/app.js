/**
 * AI Singer Studio - 前端应用
 */

// API基础URL
const API_BASE = '/api';

// ==================== 本地存储工具函数 ====================
const LocalStorageManager = {
    // 存储键名
    KEYS: {
        LLM_CONFIGS: 'ai_singer_llm_configs',
        SYNTHESIS_PROVIDERS: 'ai_singer_synthesis_providers',
        LAST_SYNC_TIME: 'ai_singer_last_sync_time'
    },
    
    /**
     * 保存LLM配置到本地存储
     */
    saveLlmConfigs(configs) {
        try {
            const data = {
                configs: configs,
                timestamp: Date.now(),
                version: '1.0'
            };
            localStorage.setItem(this.KEYS.LLM_CONFIGS, JSON.stringify(data));
            console.log('LLM配置已保存到本地存储:', configs.length, '个配置');
            return true;
        } catch (error) {
            console.error('保存LLM配置到本地存储失败:', error);
            return false;
        }
    },
    
    /**
     * 从本地存储加载LLM配置
     */
    loadLlmConfigs() {
        try {
            const data = localStorage.getItem(this.KEYS.LLM_CONFIGS);
            if (data) {
                const parsed = JSON.parse(data);
                console.log('从本地存储加载LLM配置:', parsed.configs?.length || 0, '个配置');
                return parsed.configs || [];
            }
            return null;
        } catch (error) {
            console.error('从本地存储加载LLM配置失败:', error);
            return null;
        }
    },
    
    /**
     * 保存语音合成服务配置到本地存储
     */
    saveSynthesisProviders(providers) {
        try {
            const data = {
                providers: providers,
                timestamp: Date.now(),
                version: '1.0'
            };
            localStorage.setItem(this.KEYS.SYNTHESIS_PROVIDERS, JSON.stringify(data));
            console.log('语音合成服务配置已保存到本地存储:', providers.length, '个配置');
            return true;
        } catch (error) {
            console.error('保存语音合成服务配置到本地存储失败:', error);
            return false;
        }
    },
    
    /**
     * 从本地存储加载语音合成服务配置
     */
    loadSynthesisProviders() {
        try {
            const data = localStorage.getItem(this.KEYS.SYNTHESIS_PROVIDERS);
            if (data) {
                const parsed = JSON.parse(data);
                console.log('从本地存储加载语音合成服务配置:', parsed.providers?.length || 0, '个配置');
                return parsed.providers || [];
            }
            return null;
        } catch (error) {
            console.error('从本地存储加载语音合成服务配置失败:', error);
            return null;
        }
    },
    
    /**
     * 清除所有本地存储的配置
     */
    clearAll() {
        try {
            localStorage.removeItem(this.KEYS.LLM_CONFIGS);
            localStorage.removeItem(this.KEYS.SYNTHESIS_PROVIDERS);
            localStorage.removeItem(this.KEYS.LAST_SYNC_TIME);
            console.log('已清除所有本地存储的配置');
            return true;
        } catch (error) {
            console.error('清除本地存储失败:', error);
            return false;
        }
    },
    
    /**
     * 获取本地存储的配置信息
     */
    getStorageInfo() {
        const llmData = localStorage.getItem(this.KEYS.LLM_CONFIGS);
        const synthData = localStorage.getItem(this.KEYS.SYNTHESIS_PROVIDERS);
        
        return {
            hasLlmConfigs: !!llmData,
            hasSynthesisProviders: !!synthData,
            llmConfigsCount: llmData ? JSON.parse(llmData).configs?.length || 0 : 0,
            synthesisProvidersCount: synthData ? JSON.parse(synthData).providers?.length || 0 : 0
        };
    }
};

// 全局状态
const state = {
    singers: [],
    songs: [],
    techniques: [],
    emotions: [],
    templates: [],
    projects: [],
    llmConfig: null,
    currentLlmProvider: 'qwen',
    
    // 向导状态
    wizard: {
        currentStep: 1,
        selectedSinger: null,
        songSource: 'ai',
        generatedLyrics: null,
        manualSong: null,
        selectedExistingSong: null,
        selectedTemplate: null,
        segments: []
    },
    
    // Jamendo状态
    jamendo: {
        tracks: [],
        currentPage: 1,
        pageSize: 20,
        searchQuery: '',
        filters: {
            genre: '',
            mood: '',
            vocal: '',
            speed: '',
            order: 'relevance'
        },
        isPlaying: false,
        currentTrack: null,
        audioElement: null,
        currentTime: 0,
        duration: 0,
        progressInterval: null
    }
};

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initFilters();
    loadInitialData();
});

function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const page = item.dataset.page;
            navigateTo(page);
        });
    });
}

function initFilters() {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const filterBar = btn.closest('.filter-bar');
            filterBar.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            const filter = btn.dataset.filter;
            renderWizardSingers(filter);
        });
    });
}

function navigateTo(page) {
    // 更新导航状态
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.page === page);
    });
    
    // 切换页面
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
    });
    
    const pageElement = document.getElementById(`page-${page}`);
    if (pageElement) {
        pageElement.classList.add('active');
    }
    
    // 加载页面数据
    switch(page) {
        case 'wizard':
            initWizard();
            break;
        case 'templates':
            renderTemplates();
            initTemplateFilters();
            break;
        case 'projects':
            renderProjects();
            break;
        case 'singers-manage':
            renderSingersTable();
            break;
        case 'songs-manage':
            renderSongsManage();
            break;
        case 'techniques-manage':
            renderTechniquesCards();
            break;
        case 'emotions-manage':
            renderEmotionsCards();
            break;
        case 'singing-configs':
            renderSingingConfigs();
            break;
        case 'settings':
            renderSettings();
            break;
    }
}

async function loadInitialData() {
    try {
        const [singersRes, songsRes, techniquesRes, emotionsRes, templatesRes, projectsRes, llmConfigsRes] = await Promise.all([
            fetch(`${API_BASE}/singers`),
            fetch(`${API_BASE}/songs`),
            fetch(`${API_BASE}/techniques`),
            fetch(`${API_BASE}/emotions`),
            fetch(`${API_BASE}/templates`),
            fetch(`${API_BASE}/projects`),
            fetch(`${API_BASE}/config/llm`)
        ]);
        
        const [singersData, songsData, techniquesData, emotionsData, templatesData, projectsData, llmConfigsData] = await Promise.all([
            singersRes.json(),
            songsRes.json(),
            techniquesRes.json(),
            emotionsRes.json(),
            templatesRes.json(),
            projectsRes.json(),
            llmConfigsRes.json()
        ]);
        
        state.singers = singersData.data || [];
        state.songs = songsData.data || [];
        state.techniques = techniquesData.data || [];
        state.emotions = emotionsData.data || [];
        state.templates = templatesData.data || [];
        state.projects = projectsData.data || [];
        
        // 加载LLM配置到全局
        llmConfigs = llmConfigsData.data || [];
        const activeLlm = llmConfigs.find(c => c.isActive);
        state.currentLlmProvider = activeLlm ? activeLlm.provider : 'qwen';
        state.llmConfig = { currentProvider: state.currentLlmProvider };
        
        // 加载演唱配置
        await loadSingingConfigs();
        
        // 更新所有下拉框
        updateAllLlmDropdowns();
        updateAllSingingConfigDropdowns();
        
        updateStats();
        renderRecentSongs();
        
    } catch (error) {
        console.error('加载数据失败:', error);
        showToast('加载数据失败，请刷新页面重试', 'error');
    }
}

function updateStats() {
    animateNumber('stat-singers', state.singers.length);
    animateNumber('stat-songs', state.songs.length);
    animateNumber('stat-techniques', state.techniques.length);
    animateNumber('stat-emotions', state.emotions.length);
}

function animateNumber(elementId, target) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    let current = 0;
    const increment = Math.ceil(target / 15);
    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        element.textContent = current;
    }, 50);
}

function renderRecentSongs() {
    const container = document.getElementById('recent-songs');
    if (!container) return;
    
    if (state.songs.length === 0) {
        container.innerHTML = '<p class="empty-hint">暂无歌曲，点击上方开始创建！</p>';
        return;
    }
    
    const recentSongs = state.songs.slice(0, 3);
    container.innerHTML = recentSongs.map(song => `
        <div class="song-item" style="cursor: pointer" onclick="viewSongInWizard(${song.id})">
            <div class="song-icon">${song.isGenerated ? '✨' : '🎵'}</div>
            <div class="song-info">
                <div class="song-title">${song.title}</div>
                <div class="song-meta">
                    <span>🎸 ${song.musicStyle || '未知风格'}</span>
                </div>
            </div>
        </div>
    `).join('');
}

// ========================================
// 快速开始向导
// ========================================

function initWizard() {
    state.wizard = {
        currentStep: 1,
        selectedSinger: null,
        songSource: 'ai',
        generatedLyrics: null,
        manualSong: null,
        selectedExistingSong: null,
        segments: [],
        singingConfigId: null // 演唱配置ID
    };
    
    updateWizardProgress();
    showWizardStep(1);
    renderWizardSingers('all');
    renderExistingSongs();
    updateLlmSelect();
}

function updateWizardProgress() {
    document.querySelectorAll('.progress-step').forEach(step => {
        const stepNum = parseInt(step.dataset.step);
        step.classList.remove('active', 'completed');
        
        if (stepNum < state.wizard.currentStep) {
            step.classList.add('completed');
        } else if (stepNum === state.wizard.currentStep) {
            step.classList.add('active');
        }
    });
}

function showWizardStep(step) {
    document.querySelectorAll('.wizard-step').forEach(s => s.classList.remove('active'));
    document.getElementById(`wizard-step-${step}`).classList.add('active');
    state.wizard.currentStep = step;
    updateWizardProgress();
}

function wizardNext() {
    const currentStep = state.wizard.currentStep;
    
    // 验证当前步骤
    if (currentStep === 1 && !state.wizard.selectedSinger) {
        showToast('请选择一位歌手', 'error');
        return;
    }
    
    if (currentStep === 2) {
        if (!validateStep2()) return;
        prepareSegmentsConfig();
    }
    
    if (currentStep === 3) {
        preparePreview();
    }
    
    if (currentStep < 4) {
        showWizardStep(currentStep + 1);
    }
}

function wizardPrev() {
    if (state.wizard.currentStep > 1) {
        showWizardStep(state.wizard.currentStep - 1);
    }
}

function validateStep2() {
    const source = state.wizard.songSource;
    
    if (source === 'ai') {
        if (!state.wizard.generatedLyrics) {
            showToast('请先生成歌词', 'error');
            return false;
        }
    } else if (source === 'manual') {
        const title = document.getElementById('wizard-manual-title').value.trim();
        const lyrics = document.getElementById('wizard-manual-lyrics').value.trim();
        if (!title || !lyrics) {
            showToast('请填写歌曲标题和歌词', 'error');
            return false;
        }
        state.wizard.manualSong = {
            title,
            lyrics,
            style: document.getElementById('wizard-manual-style').value,
            bpm: parseInt(document.getElementById('wizard-manual-bpm').value) || 120
        };
    } else if (source === 'existing') {
        if (!state.wizard.selectedExistingSong) {
            showToast('请选择一首歌曲', 'error');
            return false;
        }
    }
    
    return true;
}

// 歌手选择
function renderWizardSingers(filter = 'all') {
    const container = document.getElementById('wizard-singers-list');
    if (!container) return;
    
    let filteredSingers = state.singers;
    if (filter !== 'all') {
        filteredSingers = state.singers.filter(s => s.voiceType === filter);
    }
    
    container.innerHTML = filteredSingers.map(singer => `
        <div class="singer-card ${state.wizard.selectedSinger?.id === singer.id ? 'selected' : ''}" 
             onclick="selectWizardSinger(${singer.id})">
            <div class="singer-avatar">${getAvatarEmoji(singer.voiceType)}</div>
            <div class="singer-name">${singer.name}</div>
            <div class="singer-tags">
                <span class="singer-tag">${singer.voiceType}</span>
                <span class="singer-tag">${singer.voiceStyle}</span>
            </div>
            <div class="singer-desc">${singer.description || ''}</div>
        </div>
    `).join('');
}

function getAvatarEmoji(voiceType) {
    const emojis = { '女声': '👩‍🎤', '男声': '👨‍🎤', '中性': '🎭' };
    return emojis[voiceType] || '🎤';
}

function selectWizardSinger(singerId) {
    state.wizard.selectedSinger = state.singers.find(s => s.id === singerId);
    renderWizardSingers(document.querySelector('.filter-btn.active')?.dataset.filter || 'all');
    document.getElementById('btn-step1-next').disabled = false;
}

// 歌曲来源切换
function switchSongSource(source) {
    state.wizard.songSource = source;
    
    document.querySelectorAll('.source-tab').forEach(tab => {
        tab.classList.toggle('active', tab.onclick.toString().includes(`'${source}'`));
    });
    
    document.querySelectorAll('.song-source-panel').forEach(panel => {
        panel.classList.remove('active');
    });
    document.getElementById(`source-${source}`).classList.add('active');
}

// AI生成歌词
function updateLlmSelect() {
    const select = document.getElementById('wizard-llm-select');
    if (select) {
        select.value = state.currentLlmProvider;
    }
}

async function wizardSwitchLlm(configIdOrProvider) {
    // 兼容旧的provider方式和新的configId方式
    if (configIdOrProvider && !isNaN(configIdOrProvider)) {
        // 这是configId
        await onLlmSelectChange(configIdOrProvider);
    } else {
        // 这是provider名称，找到对应的configId
        const config = llmConfigs.find(c => c.provider === configIdOrProvider);
        if (config) {
            await onLlmSelectChange(config.id);
        }
    }
}

async function wizardGenerateLyrics() {
    const theme = document.getElementById('wizard-theme').value.trim();
    if (!theme) {
        showToast('请输入创作主题', 'error');
        return;
    }
    
    showLoading(true, 'AI正在创作歌词...');
    
    try {
        const response = await fetch(`${API_BASE}/songs/generate-lyrics`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                theme,
                mood: document.getElementById('wizard-mood').value,
                style: document.getElementById('wizard-style').value,
                keywords: document.getElementById('wizard-keywords').value,
                language: '中文',
                hasChorus: true
            })
        });
        
        const data = await response.json();
        
        if (data.success && data.data) {
            state.wizard.generatedLyrics = data.data;
            displayWizardLyrics(data.data);
            showToast('歌词生成成功！', 'success');
        } else {
            showToast(data.message || '生成失败', 'error');
        }
    } catch (error) {
        console.error('生成失败:', error);
        showToast('生成失败，请稍后重试', 'error');
    } finally {
        showLoading(false);
    }
}

function displayWizardLyrics(lyrics) {
    const preview = document.getElementById('wizard-lyrics-preview');
    preview.classList.remove('hidden');
    
    document.getElementById('wizard-song-title').textContent = lyrics.title || '未命名歌曲';
    document.getElementById('wizard-lyrics-content').textContent = lyrics.fullLyrics || '';
}

// 现有歌曲选择
function renderExistingSongs() {
    const container = document.getElementById('wizard-existing-songs');
    if (!container) return;
    
    if (state.songs.length === 0) {
        container.innerHTML = '<p class="empty-hint">暂无歌曲</p>';
        return;
    }
    
    container.innerHTML = state.songs.map(song => `
        <div class="song-item ${state.wizard.selectedExistingSong?.id === song.id ? 'selected' : ''}"
             onclick="selectExistingSong(${song.id})"
             style="cursor: pointer; ${state.wizard.selectedExistingSong?.id === song.id ? 'border-color: var(--accent-primary);' : ''}">
            <div class="song-icon">🎵</div>
            <div class="song-info">
                <div class="song-title">${song.title}</div>
                <div class="song-meta">
                    <span>🎸 ${song.musicStyle || '未知'}</span>
                    <span>🥁 ${song.bpm || '--'} BPM</span>
                </div>
            </div>
        </div>
    `).join('');
}

function selectExistingSong(songId) {
    state.wizard.selectedExistingSong = state.songs.find(s => s.id === songId);
    renderExistingSongs();
}

// 片段配置
function prepareSegmentsConfig() {
    const container = document.getElementById('wizard-segments-config');
    let lyrics = '';
    let sections = [];
    
    if (state.wizard.songSource === 'ai' && state.wizard.generatedLyrics) {
        sections = state.wizard.generatedLyrics.sections || [];
    } else if (state.wizard.songSource === 'manual' && state.wizard.manualSong) {
        // 将手动输入的歌词按段落分割
        const parts = state.wizard.manualSong.lyrics.split(/\n\n+/);
        sections = parts.map((content, i) => ({
            type: i === 0 ? 'verse' : (i === parts.length - 1 ? 'outro' : 'verse'),
            content: content.trim(),
            suggestedEmotion: '自然',
            suggestedTechnique: '自然'
        }));
    } else if (state.wizard.songSource === 'existing' && state.wizard.selectedExistingSong) {
        // 使用现有歌曲的歌词
        const song = state.wizard.selectedExistingSong;
        if (song.lyrics) {
            const parts = song.lyrics.split(/\n\n+/);
            sections = parts.map((content, i) => ({
                type: 'verse',
                content: content.trim(),
                suggestedEmotion: '自然',
                suggestedTechnique: '自然'
            }));
        }
    }
    
    state.wizard.segments = sections.map((section, index) => ({
        ...section,
        techniqueId: null,
        emotionId: null
    }));
    
    container.innerHTML = state.wizard.segments.map((segment, index) => `
        <div class="segment-config-item">
            <div class="segment-type">${getSegmentTypeName(segment.type)}</div>
            <div class="segment-lyrics-short">${segment.content.substring(0, 30)}...</div>
            <select onchange="updateSegmentTechnique(${index}, this.value)">
                <option value="">选择技巧...</option>
                ${state.techniques.map(t => `
                    <option value="${t.id}">${t.name}</option>
                `).join('')}
            </select>
            <select onchange="updateSegmentEmotion(${index}, this.value)">
                <option value="">选择情绪...</option>
                ${state.emotions.map(e => `
                    <option value="${e.id}">${e.iconName || ''} ${e.name}</option>
                `).join('')}
            </select>
        </div>
    `).join('');
}

function getSegmentTypeName(type) {
    const names = {
        'intro': '前奏',
        'verse': '主歌',
        'chorus': '副歌',
        'bridge': '桥段',
        'outro': '尾声'
    };
    return names[type] || type || '段落';
}

function updateSegmentTechnique(index, techniqueId) {
    state.wizard.segments[index].techniqueId = techniqueId ? parseInt(techniqueId) : null;
}

function updateSegmentEmotion(index, emotionId) {
    state.wizard.segments[index].emotionId = emotionId ? parseInt(emotionId) : null;
}

// 预览
function preparePreview() {
    // 歌手预览
    document.getElementById('preview-singer').textContent = 
        state.wizard.selectedSinger?.name || '-';
    
    // 歌曲预览
    let songTitle = '-';
    let songStyle = '-';
    
    if (state.wizard.songSource === 'ai' && state.wizard.generatedLyrics) {
        songTitle = state.wizard.generatedLyrics.title;
        songStyle = state.wizard.generatedLyrics.suggestedStyle || '流行';
    } else if (state.wizard.songSource === 'manual' && state.wizard.manualSong) {
        songTitle = state.wizard.manualSong.title;
        songStyle = state.wizard.manualSong.style || '流行';
    } else if (state.wizard.songSource === 'existing' && state.wizard.selectedExistingSong) {
        songTitle = state.wizard.selectedExistingSong.title;
        songStyle = state.wizard.selectedExistingSong.musicStyle || '流行';
    }
    
    document.getElementById('preview-song').textContent = songTitle;
    document.getElementById('preview-style').textContent = songStyle;
    
    // 演唱配置预览
    let singingConfigName = '未选择';
    if (state.wizard.singingConfigId) {
        const selectedConfig = singingConfigs.find(c => c.id == state.wizard.singingConfigId);
        if (selectedConfig) {
            singingConfigName = `${selectedConfig.name} (${selectedConfig.category || '通用'})`;
        }
    }
    document.getElementById('preview-singing-config').textContent = singingConfigName;
    
    // 片段预览
    const container = document.getElementById('preview-segments');
    container.innerHTML = state.wizard.segments.map(segment => {
        const technique = state.techniques.find(t => t.id === segment.techniqueId);
        const emotion = state.emotions.find(e => e.id === segment.emotionId);
        
        return `
            <div class="preview-segment">
                <span class="preview-segment-type">${getSegmentTypeName(segment.type)}</span>
                <div class="preview-segment-config">
                    <span>🎤 ${technique?.name || '未设置'}</span>
                    <span>${emotion?.iconName || '😊'} ${emotion?.name || '未设置'}</span>
                </div>
            </div>
        `;
    }).join('');
}

// 完成向导
async function wizardFinish() {
    showLoading(true, '正在保存...');
    
    try {
        let songData;
        
        if (state.wizard.songSource === 'ai' && state.wizard.generatedLyrics) {
            const lyrics = state.wizard.generatedLyrics;
            songData = {
                title: lyrics.title,
                lyrics: lyrics.fullLyrics,
                musicStyle: lyrics.suggestedStyle || '流行',
                bpm: lyrics.suggestedBpm || 120,
                singerId: state.wizard.selectedSinger?.id,
                singingConfigId: state.wizard.singingConfigId, // 演唱配置ID
                segments: state.wizard.segments.map((seg, i) => ({
                    segmentOrder: i + 1,
                    segmentType: getSegmentTypeName(seg.type),
                    lyrics: seg.content,
                    techniqueId: seg.techniqueId,
                    emotionId: seg.emotionId
                }))
            };
        } else if (state.wizard.songSource === 'manual') {
            const manual = state.wizard.manualSong;
            songData = {
                title: manual.title,
                lyrics: manual.lyrics,
                musicStyle: manual.style,
                bpm: manual.bpm,
                singerId: state.wizard.selectedSinger?.id,
                singingConfigId: state.wizard.singingConfigId, // 演唱配置ID
                segments: state.wizard.segments.map((seg, i) => ({
                    segmentOrder: i + 1,
                    segmentType: getSegmentTypeName(seg.type),
                    lyrics: seg.content,
                    techniqueId: seg.techniqueId,
                    emotionId: seg.emotionId
                }))
            };
        } else {
            // 对于现有歌曲，只需要更新片段配置
            showToast('歌曲配置已保存！', 'success');
            navigateTo('home');
            loadInitialData();
            return;
        }
        
        const response = await fetch(`${API_BASE}/songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(songData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('🎉 歌曲创建成功！', 'success');
            navigateTo('home');
            loadInitialData();
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存失败:', error);
        showToast('保存失败', 'error');
    } finally {
        showLoading(false);
    }
}

// ========================================
// 后台管理：歌手管理
// ========================================

function renderSingersTable() {
    const tbody = document.getElementById('singers-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = state.singers.map(singer => `
        <tr>
            <td class="avatar-cell">
                <div class="table-avatar">${getAvatarEmoji(singer.voiceType)}</div>
            </td>
            <td><strong>${singer.name}</strong></td>
            <td>${singer.voiceType || '-'}</td>
            <td>${singer.voiceStyle || '-'}</td>
            <td>${singer.description || '-'}</td>
            <td>
                <span class="status-badge ${singer.enabled ? 'active' : 'inactive'}">
                    ${singer.enabled ? '启用' : '禁用'}
                </span>
            </td>
            <td>
                <div class="table-actions">
                    <button class="btn-icon" onclick="editSinger(${singer.id})" title="编辑">✏️</button>
                    <button class="btn-icon danger" onclick="deleteSinger(${singer.id})" title="删除">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// ========================================
// 后台管理：歌曲管理
// ========================================

// 歌曲管理状态
const songsManageState = {
    currentSource: 'local',
    localFilter: 'all',
    localSearch: '',
    jamendo: {
        tracks: [],
        currentPage: 1,
        pageSize: 12,
        searchQuery: '',
        genre: '',
        mood: '',
        order: 'popularity_total'
    }
};

function renderSongsManage() {
    // 更新本地歌曲计数
    const countEl = document.getElementById('local-songs-count');
    if (countEl) {
        countEl.textContent = state.songs.length;
    }
    
    // 渲染本地歌曲列表
    renderLocalSongsList();
    
    // 如果当前在Jamendo面板且没有数据，加载热门歌曲
    if (songsManageState.currentSource === 'jamendo' && songsManageState.jamendo.tracks.length === 0) {
        loadSongsJamendo('popular');
    }
}

function switchSongsSource(source) {
    songsManageState.currentSource = source;
    
    // 更新标签状态
    document.querySelectorAll('.source-tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.source === source);
    });
    
    // 切换面板
    document.querySelectorAll('.songs-panel').forEach(panel => {
        panel.classList.remove('active');
    });
    document.getElementById(`songs-panel-${source}`).classList.add('active');
    
    // 加载数据
    if (source === 'jamendo' && songsManageState.jamendo.tracks.length === 0) {
        loadSongsJamendo('popular');
    }
}

function renderLocalSongsList() {
    const container = document.getElementById('songs-manage-list');
    if (!container) return;
    
    let filteredSongs = state.songs;
    
    // 应用风格筛选
    if (songsManageState.localFilter !== 'all') {
        filteredSongs = filteredSongs.filter(s => s.musicStyle === songsManageState.localFilter);
    }
    
    // 应用搜索筛选
    if (songsManageState.localSearch) {
        const search = songsManageState.localSearch.toLowerCase();
        filteredSongs = filteredSongs.filter(s => 
            s.title?.toLowerCase().includes(search) ||
            s.lyrics?.toLowerCase().includes(search)
        );
    }
    
    if (filteredSongs.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🎵</div>
                <div class="empty-state-text">暂无歌曲</div>
                <div class="empty-state-hint">点击"创建歌曲"或从Jamendo导入</div>
            </div>
        `;
        return;
    }
    
    container.innerHTML = filteredSongs.map(song => `
        <div class="song-item">
            <div class="song-icon">${getSongIcon(song)}</div>
            <div class="song-info">
                <div class="song-title">${song.title}</div>
                <div class="song-meta">
                    <span>🎸 ${song.musicStyle || '未知'}</span>
                    <span>🥁 ${song.bpm || '--'} BPM</span>
                    ${song.isGenerated ? '<span>🤖 AI生成</span>' : ''}
                    ${song.externalSource === 'jamendo' ? '<span>🌐 Jamendo</span>' : ''}
                    ${song.artist ? `<span>👤 ${song.artist}</span>` : ''}
                </div>
            </div>
            <div class="table-actions">
                ${song.audioUrl ? `<button class="btn-icon" onclick="playSong('${song.audioUrl}')" title="播放">▶️</button>` : ''}
                <button class="btn-icon" onclick="editSongSegments(${song.id})" title="编辑片段">🎚️</button>
                <button class="btn-icon danger" onclick="deleteSong(${song.id})" title="删除">🗑️</button>
            </div>
        </div>
    `).join('');
}

function getSongIcon(song) {
    if (song.externalSource === 'jamendo') return '🌐';
    if (song.isGenerated) return '✨';
    return '🎵';
}

function filterLocalSongs() {
    const searchInput = document.getElementById('local-songs-search');
    songsManageState.localSearch = searchInput?.value?.trim() || '';
    renderLocalSongsList();
}

function filterLocalByStyle(style) {
    songsManageState.localFilter = style;
    
    document.querySelectorAll('#songs-panel-local .filter-pill').forEach(pill => {
        pill.classList.toggle('active', pill.dataset.filter === style);
    });
    
    renderLocalSongsList();
}

// Jamendo歌曲管理集成
async function loadSongsJamendo(type = 'search') {
    const container = document.getElementById('songs-jamendo-list');
    container.innerHTML = `
        <div class="loading-placeholder">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
        </div>
    `;
    
    try {
        let url = `${API_BASE}/jamendo/`;
        const params = new URLSearchParams();
        params.append('limit', songsManageState.jamendo.pageSize);
        params.append('offset', (songsManageState.jamendo.currentPage - 1) * songsManageState.jamendo.pageSize);
        
        if (type === 'popular') {
            url += 'popular';
        } else if (type === 'latest') {
            url += 'latest';
        } else if (type === 'genre' && songsManageState.jamendo.genre) {
            url += `genre/${songsManageState.jamendo.genre}`;
        } else {
            url += 'search';
            if (songsManageState.jamendo.searchQuery) {
                params.append('search', songsManageState.jamendo.searchQuery);
            }
            if (songsManageState.jamendo.genre) {
                params.append('tags', songsManageState.jamendo.genre);
            }
            if (songsManageState.jamendo.mood) {
                params.append('mood', songsManageState.jamendo.mood);
            }
            if (songsManageState.jamendo.order) {
                params.append('orderBy', songsManageState.jamendo.order);
            }
        }
        
        url += '?' + params.toString();
        
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.success && data.data) {
            // 规范化字段名，确保兼容snake_case和camelCase
            songsManageState.jamendo.tracks = data.data.map(track => ({
                ...track,
                artistName: track.artistName || track.artist_name || '未知艺术家',
                albumName: track.albumName || track.album_name || null,
                name: track.name || track.title || '未知歌曲'
            }));
            renderSongsJamendoList();
            updateSongsJamendoPagination();
        } else {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🎵</div>
                    <div class="empty-state-text">未找到歌曲</div>
                    <div class="empty-state-hint">请检查Jamendo配置或尝试其他搜索条件</div>
                </div>
            `;
        }
    } catch (error) {
        console.error('加载Jamendo歌曲失败:', error);
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <div class="empty-state-text">加载失败</div>
                <div class="empty-state-hint">请检查网络连接</div>
            </div>
        `;
    }
}

function renderSongsJamendoList() {
    const container = document.getElementById('songs-jamendo-list');
    
    if (songsManageState.jamendo.tracks.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🔍</div>
                <div class="empty-state-text">未找到歌曲</div>
            </div>
        `;
        return;
    }
    
    const isCurrentTrack = (trackId) => state.jamendo.currentTrack?.id === trackId;
    const isPlaying = (trackId) => isCurrentTrack(trackId) && state.jamendo.isPlaying;
    
    container.innerHTML = songsManageState.jamendo.tracks.map(track => {
        const isActive = isCurrentTrack(track.id);
        const playing = isPlaying(track.id);
        const progress = isActive && state.jamendo.duration > 0 
            ? (state.jamendo.currentTime / state.jamendo.duration) * 100 
            : 0;
        
        return `
        <div class="jamendo-track-compact ${isActive ? 'track-playing' : ''}" data-track-id="${track.id}">
            <div class="track-thumb">
                ${track.image ? 
                    `<img src="${track.image}" alt="${track.name || '未知歌曲'}" loading="lazy">` : 
                    `<div class="track-thumb-placeholder">🎵</div>`
                }
                <div class="play-overlay ${playing ? 'playing' : ''}" onclick="playJamendoInSongs('${track.id}')">
                    <span>${playing ? '⏸' : '▶'}</span>
                </div>
            </div>
            <div class="track-main">
                <div class="track-title-sm" title="${track.name || '未知歌曲'}">
                    ${track.name || '未知歌曲'}
                    ${playing ? '<span class="playing-indicator">●</span>' : ''}
                </div>
                <div class="track-artist-sm" title="${track.artistName || '未知艺术家'}">${track.artistName || '未知艺术家'}</div>
                <div class="track-meta-sm">
                    <span>⏱ ${formatDuration(track.duration)}</span>
                    ${track.albumName ? `<span>💿 ${track.albumName}</span>` : ''}
                </div>
                ${isActive ? `
                    <div class="track-progress-container-compact">
                        <div class="track-progress-bar-compact">
                            <div class="track-progress-fill-compact" style="width: ${progress}%"></div>
                        </div>
                        <div class="track-progress-time-compact">
                            <span>${formatDuration(state.jamendo.currentTime)}</span>
                            <span>/</span>
                            <span>${formatDuration(state.jamendo.duration || track.duration)}</span>
                        </div>
                    </div>
                ` : ''}
            </div>
            <div class="track-actions-sm">
                <button class="btn-action-sm" onclick="previewJamendoInSongs('${track.id}')" title="试听">
                    🎧
                </button>
                <button class="btn-action-sm primary" onclick="importJamendoInSongs('${track.id}')" title="导入到本地">
                    📥 导入
                </button>
            </div>
        </div>
        `;
    }).join('');
}

function updateSongsJamendoPagination() {
    const prevBtn = document.getElementById('songs-jamendo-prev');
    const nextBtn = document.getElementById('songs-jamendo-next');
    const pageInfo = document.getElementById('songs-jamendo-page-info');
    
    if (prevBtn) prevBtn.disabled = songsManageState.jamendo.currentPage <= 1;
    if (nextBtn) nextBtn.disabled = songsManageState.jamendo.tracks.length < songsManageState.jamendo.pageSize;
    if (pageInfo) pageInfo.textContent = `第 ${songsManageState.jamendo.currentPage} 页`;
}

function songsJamendoPage(delta) {
    songsManageState.jamendo.currentPage = Math.max(1, songsManageState.jamendo.currentPage + delta);
    loadSongsJamendo('search');
}

function searchSongsJamendo() {
    const searchInput = document.getElementById('songs-jamendo-search');
    songsManageState.jamendo.searchQuery = searchInput?.value?.trim() || '';
    songsManageState.jamendo.currentPage = 1;
    loadSongsJamendo('search');
}

function filterSongsJamendo() {
    songsManageState.jamendo.genre = document.getElementById('songs-jamendo-genre')?.value || '';
    songsManageState.jamendo.mood = document.getElementById('songs-jamendo-mood')?.value || '';
    songsManageState.jamendo.order = document.getElementById('songs-jamendo-order')?.value || 'popularity_total';
    songsManageState.jamendo.currentPage = 1;
    loadSongsJamendo('search');
}

function quickJamendoSearch(type) {
    // 重置筛选
    songsManageState.jamendo.searchQuery = '';
    songsManageState.jamendo.currentPage = 1;
    songsManageState.jamendo.genre = '';
    songsManageState.jamendo.mood = '';
    
    // 重置UI
    const searchInput = document.getElementById('songs-jamendo-search');
    const genreSelect = document.getElementById('songs-jamendo-genre');
    const moodSelect = document.getElementById('songs-jamendo-mood');
    
    if (searchInput) searchInput.value = '';
    if (genreSelect) genreSelect.value = '';
    if (moodSelect) moodSelect.value = '';
    
    if (type === 'popular') {
        loadSongsJamendo('popular');
    } else if (type === 'latest') {
        loadSongsJamendo('latest');
    } else {
        songsManageState.jamendo.genre = type;
        if (genreSelect) genreSelect.value = type;
        loadSongsJamendo('genre');
    }
}

function playJamendoInSongs(trackId) {
    const track = songsManageState.jamendo.tracks.find(t => t.id === trackId);
    if (!track) return;
    
    // 规范化字段名
    const normalizedTrack = {
        ...track,
        artistName: track.artistName || track.artist_name || '未知艺术家',
        albumName: track.albumName || track.album_name || null,
        name: track.name || track.title || '未知歌曲'
    };
    
    // 如果正在播放同一首歌曲，则暂停
    if (state.jamendo.currentTrack?.id === trackId && state.jamendo.isPlaying) {
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement.pause();
            state.jamendo.isPlaying = false;
            stopSongsProgressUpdate();
            renderSongsJamendoList();
            showToast('已暂停', 'info');
        }
        return;
    }
    
    // 如果暂停状态，恢复播放
    if (state.jamendo.currentTrack?.id === trackId && !state.jamendo.isPlaying) {
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement.play().then(() => {
                state.jamendo.isPlaying = true;
                startSongsProgressUpdate();
                renderSongsJamendoList();
            }).catch(err => {
                console.error('恢复播放失败:', err);
                showToast('恢复播放失败', 'error');
            });
        }
        return;
    }
    
    // 停止当前播放
    if (state.jamendo.audioElement) {
        state.jamendo.audioElement.pause();
        state.jamendo.audioElement = null;
    }
    
    state.jamendo.currentTrack = normalizedTrack;
    state.jamendo.audioElement = new Audio(normalizedTrack.audio);
    state.jamendo.audioElement.volume = 0.7;
    
    // 添加事件监听
    state.jamendo.audioElement.onloadedmetadata = () => {
        state.jamendo.duration = state.jamendo.audioElement.duration;
        renderSongsJamendoList();
    };
    
    state.jamendo.audioElement.ontimeupdate = () => {
        state.jamendo.currentTime = state.jamendo.audioElement.currentTime;
        updateSongsTrackProgress();
    };
    
    state.jamendo.audioElement.onplay = () => {
        state.jamendo.isPlaying = true;
        startSongsProgressUpdate();
        renderSongsJamendoList();
    };
    
    state.jamendo.audioElement.onpause = () => {
        state.jamendo.isPlaying = false;
        stopSongsProgressUpdate();
        renderSongsJamendoList();
    };
    
    state.jamendo.audioElement.onended = () => {
        state.jamendo.isPlaying = false;
        state.jamendo.currentTime = 0;
        state.jamendo.duration = 0;
        state.jamendo.currentTrack = null;
        stopSongsProgressUpdate();
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement = null;
        }
        renderSongsJamendoList();
    };
    
    state.jamendo.audioElement.onerror = () => {
        state.jamendo.isPlaying = false;
        state.jamendo.currentTime = 0;
        state.jamendo.duration = 0;
        state.jamendo.currentTrack = null;
        stopSongsProgressUpdate();
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement = null;
        }
        renderSongsJamendoList();
        showToast('播放失败', 'error');
    };
    
    state.jamendo.audioElement.play().then(() => {
        state.jamendo.isPlaying = true;
        showToast(`正在播放: ${normalizedTrack.name}`, 'success');
    }).catch(err => {
        console.error('播放失败:', err);
        state.jamendo.isPlaying = false;
        state.jamendo.currentTime = 0;
        state.jamendo.duration = 0;
        state.jamendo.currentTrack = null;
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement = null;
        }
        stopSongsProgressUpdate();
        renderSongsJamendoList();
        showToast('播放失败', 'error');
    });
}

function previewJamendoInSongs(trackId) {
    playJamendoInSongs(trackId);
}

async function importJamendoInSongs(trackId) {
    const track = songsManageState.jamendo.tracks.find(t => t.id === trackId);
    if (!track) return;
    
    // 检查是否已导入
    const exists = state.songs.find(s => s.externalId === track.id && s.externalSource === 'jamendo');
    if (exists) {
        showToast('该歌曲已在本地歌曲库中', 'info');
        return;
    }
    
    showLoading(true, '正在导入歌曲...');
    
    try {
        const songData = {
            title: track.name,
            lyrics: track.lyrics || `(Jamendo导入)\n\n艺术家: ${track.artistName}\n专辑: ${track.albumName || 'Single'}`,
            musicStyle: track.musicinfo?.tags?.genres?.[0] || '流行',
            bpm: 120,
            isGenerated: false,
            externalSource: 'jamendo',
            externalId: track.id,
            externalUrl: track.shareurl,
            audioUrl: track.audio,
            coverUrl: track.image,
            artist: track.artistName,
            album: track.albumName,
            duration: track.duration,
            license: track.licenseCcurl
        };
        
        const response = await fetch(`${API_BASE}/songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(songData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            state.songs.push(data.data);
            
            // 更新本地歌曲计数
            const countEl = document.getElementById('local-songs-count');
            if (countEl) countEl.textContent = state.songs.length;
            
            showToast(`✅ "${track.name}" 已导入本地歌曲库`, 'success');
        } else {
            showToast(data.message || '导入失败', 'error');
        }
    } catch (error) {
        console.error('导入失败:', error);
        showToast('导入失败', 'error');
    } finally {
        showLoading(false);
    }
}

function playSong(audioUrl) {
    if (state.jamendo.audioElement) {
        state.jamendo.audioElement.pause();
    }
    state.jamendo.audioElement = new Audio(audioUrl);
    state.jamendo.audioElement.volume = 0.7;
    state.jamendo.audioElement.play().then(() => {
        showToast('正在播放...', 'success');
    }).catch(err => {
        showToast('播放失败', 'error');
    });
}

async function deleteSong(songId) {
    if (!confirm('确定要删除这首歌曲吗？')) return;
    
    try {
        await fetch(`${API_BASE}/songs/${songId}`, { method: 'DELETE' });
        state.songs = state.songs.filter(s => s.id !== songId);
        renderSongsManage();
        updateStats();
        showToast('歌曲已删除', 'success');
    } catch (error) {
        showToast('删除失败', 'error');
    }
}

// ========================================
// 后台管理：技巧管理
// ========================================

function renderTechniquesCards() {
    const container = document.getElementById('techniques-cards');
    if (!container) return;
    
    container.innerHTML = state.techniques.map(tech => `
        <div class="config-card ${tech.enabled ? '' : 'disabled'}">
            <div class="config-card-header">
                <div class="config-card-title">
                    <div class="config-card-icon">🎤</div>
                    <div>
                        <div class="config-card-name">${tech.name}</div>
                        <div class="config-card-name-en">${tech.nameEn || ''}</div>
                    </div>
                </div>
                <span class="config-card-status ${tech.enabled ? 'enabled' : 'disabled'}">
                    ${tech.enabled ? '启用' : '禁用'}
                </span>
            </div>
            <div class="config-card-body">
                <div class="config-card-description">${tech.description || '暂无描述'}</div>
                <div class="config-card-meta">
                    <span class="config-meta-tag">📁 ${tech.category || '未分类'}</span>
                    <span class="config-meta-tag">⭐ 难度 ${tech.difficultyLevel || 1}</span>
                    <span class="config-meta-tag">🔊 ${tech.phonationType || 'normal'}</span>
                </div>
                <div class="config-card-params">
                    <div class="config-param-item">
                        <div class="config-param-label">颤音</div>
                        <div class="config-param-value">${tech.vibratoDepth || 50}</div>
                    </div>
                    <div class="config-param-item">
                        <div class="config-param-label">气声</div>
                        <div class="config-param-value">${tech.breathiness || 30}</div>
                    </div>
                    <div class="config-param-item">
                        <div class="config-param-label">张力</div>
                        <div class="config-param-value">${tech.tension || 50}</div>
                    </div>
                </div>
            </div>
            <div class="config-card-footer">
                <button class="btn-preview" onclick="previewTechnique(${tech.id})">🔊 试听</button>
                <button class="btn-edit" onclick="editTechnique(${tech.id})">✏️ 编辑</button>
                <button class="btn-delete" onclick="confirmDeleteTechnique(${tech.id})">🗑️ 删除</button>
            </div>
        </div>
    `).join('');
}

/**
 * 试听技巧配置效果
 */
async function previewTechnique(id) {
    const technique = state.techniques.find(t => t.id === id);
    if (!technique) return;
    
    showToast('正在生成试听...', 'info');
    
    try {
        const response = await fetch(`${API_BASE}/synthesis/preview`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                text: `这是${technique.name}技巧的试听效果。${technique.description || ''}`,
                vibratoDepth: technique.vibratoDepth || 50,
                vibratoRate: technique.vibratoRate || 50,
                breathiness: technique.breathiness || 30,
                tension: technique.tension || 50,
                brightness: technique.brightness || 50,
                techniqueId: technique.id
            })
        });
        
        const data = await response.json();
        
        if (data.success && data.data.audioUrl) {
            playPreviewAudio(data.data.audioUrl, `技巧: ${technique.name}`);
        } else {
            showToast(data.message || '试听生成失败，请配置OpenAI API Key', 'error');
        }
    } catch (error) {
        console.error('试听失败:', error);
        showToast('试听失败，请检查网络或API配置', 'error');
    }
}

/**
 * 播放预览音频
 */
function playPreviewAudio(audioUrl, title) {
    // 创建或获取音频播放器
    let player = document.getElementById('preview-audio-player');
    if (!player) {
        player = document.createElement('div');
        player.id = 'preview-audio-player';
        player.className = 'preview-audio-player';
        player.innerHTML = `
            <div class="player-header">
                <span class="player-title"></span>
                <button class="player-close" onclick="closePreviewPlayer()">×</button>
            </div>
            <audio controls autoplay></audio>
        `;
        document.body.appendChild(player);
    }
    
    player.querySelector('.player-title').textContent = title;
    const audio = player.querySelector('audio');
    audio.src = audioUrl;
    audio.play();
    player.classList.add('show');
}

function closePreviewPlayer() {
    const player = document.getElementById('preview-audio-player');
    if (player) {
        const audio = player.querySelector('audio');
        audio.pause();
        player.classList.remove('show');
    }
}

// ========================================
// 后台管理：情绪管理
// ========================================

function renderEmotionsCards() {
    const container = document.getElementById('emotions-cards');
    if (!container) return;
    
    container.innerHTML = state.emotions.map(emotion => `
        <div class="config-card ${emotion.enabled ? '' : 'disabled'}" style="border-left: 4px solid ${emotion.colorCode || '#7b2ff7'}">
            <div class="config-card-header">
                <div class="config-card-title">
                    <div class="config-card-icon" style="background: ${emotion.colorCode || '#7b2ff7'}">${emotion.iconName || '😊'}</div>
                    <div>
                        <div class="config-card-name">${emotion.name}</div>
                        <div class="config-card-name-en">${emotion.nameEn || ''}</div>
                    </div>
                </div>
                <span class="config-card-status ${emotion.enabled ? 'enabled' : 'disabled'}">
                    ${emotion.enabled ? '启用' : '禁用'}
                </span>
            </div>
            <div class="config-card-body">
                <div class="config-card-description">${emotion.description || '暂无描述'}</div>
                <div class="config-card-meta">
                    <span class="config-meta-tag">📁 ${emotion.category || '未分类'}</span>
                    <span class="config-meta-tag">💪 强度 ${emotion.intensity || 50}%</span>
                </div>
                <div class="config-card-params">
                    <div class="config-param-item">
                        <div class="config-param-label">音高变化</div>
                        <div class="config-param-value">${emotion.pitchVariance || 1.0}</div>
                    </div>
                    <div class="config-param-item">
                        <div class="config-param-label">能量</div>
                        <div class="config-param-value">${emotion.energyMultiplier || 1.0}</div>
                    </div>
                    <div class="config-param-item">
                        <div class="config-param-label">节奏</div>
                        <div class="config-param-value">${emotion.tempoFactor || 1.0}</div>
                    </div>
                </div>
            </div>
            <div class="config-card-footer">
                <button class="btn-preview" onclick="previewEmotion(${emotion.id})">🔊 试听</button>
                <button class="btn-edit" onclick="editEmotion(${emotion.id})">✏️ 编辑</button>
                <button class="btn-delete" onclick="confirmDeleteEmotion(${emotion.id})">🗑️ 删除</button>
            </div>
        </div>
    `).join('');
}

/**
 * 试听情绪配置效果
 */
async function previewEmotion(id) {
    const emotion = state.emotions.find(e => e.id === id);
    if (!emotion) return;
    
    showToast('正在生成试听...', 'info');
    
    try {
        const response = await fetch(`${API_BASE}/synthesis/preview`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                text: `这是${emotion.name}情绪的试听效果。${emotion.description || ''}`,
                emotionIntensity: emotion.intensity || 50,
                tempoFactor: emotion.tempoFactor || 1.0,
                emotionId: emotion.id
            })
        });
        
        const data = await response.json();
        
        if (data.success && data.data.audioUrl) {
            playPreviewAudio(data.data.audioUrl, `情绪: ${emotion.name}`);
        } else {
            showToast(data.message || '试听生成失败，请配置OpenAI API Key', 'error');
        }
    } catch (error) {
        console.error('试听失败:', error);
        showToast('试听失败，请检查网络或API配置', 'error');
    }
}

// ========================================
// 后台管理：系统设置
// ========================================

// 系统配置状态
let llmConfigs = [];
let jamendoConfig = null;

// ==================== 全局配置下拉框管理 ====================

/**
 * 更新所有LLM下拉框
 * 实现"一次配置，到处使用"
 */
function updateAllLlmDropdowns() {
    const dropdownIds = [
        'wizard-llm-select',      // 创作向导
        'ai-singer-llm-select'    // AI创建歌手
    ];
    
    const icons = { 'qwen': '🔮', 'openai': '🤖', 'gemini': '💎' };
    const activeConfig = llmConfigs.find(c => c.isActive);
    
    dropdownIds.forEach(id => {
        const dropdown = document.getElementById(id);
        if (!dropdown) return;
        
        dropdown.innerHTML = llmConfigs
            .filter(c => c.enabled)
            .map(config => {
                const icon = icons[config.provider] || '🔧';
                const hasKey = config.apiKey && config.apiKey.length > 0;
                const statusText = hasKey ? '' : ' (未配置密钥)';
                const isActive = config.isActive;
                
                return `<option value="${config.id}" ${isActive ? 'selected' : ''} ${!hasKey ? 'disabled' : ''}>
                    ${icon} ${config.displayName || config.provider}${statusText}
                </option>`;
            }).join('');
    });
}

/**
 * LLM下拉框选择变化时的处理
 */
async function onLlmSelectChange(configId) {
    if (!configId) return;
    
    try {
        const response = await fetch(`${API_BASE}/config/llm/${configId}/activate`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            // 更新本地状态
            llmConfigs.forEach(c => c.isActive = (c.id == configId));
            state.currentLlmProvider = data.data.provider;
            
            // 同步所有下拉框
            updateAllLlmDropdowns();
            
            showToast(`已切换到 ${data.data.displayName}`, 'success');
        } else {
            showToast(data.message || '切换失败', 'error');
            // 恢复下拉框状态
            updateAllLlmDropdowns();
        }
    } catch (error) {
        console.error('切换LLM失败:', error);
        showToast('切换失败', 'error');
        updateAllLlmDropdowns();
    }
}

/**
 * 获取当前激活的LLM配置ID
 */
function getActiveLlmConfigId() {
    const activeConfig = llmConfigs.find(c => c.isActive);
    return activeConfig ? activeConfig.id : null;
}

/**
 * 渲染歌手选择下拉框
 */
function renderSingerDropdown(selectId, selectedId = null) {
    const dropdown = document.getElementById(selectId);
    if (!dropdown) return;
    
    dropdown.innerHTML = '<option value="">请选择歌手</option>' + 
        state.singers
            .filter(s => s.enabled)
            .map(singer => {
                const emoji = getAvatarEmoji(singer.voiceType);
                return `<option value="${singer.id}" ${singer.id == selectedId ? 'selected' : ''}>
                    ${emoji} ${singer.name} (${singer.voiceType || '未知'})
                </option>`;
            }).join('');
}

/**
 * 渲染技巧选择下拉框
 */
function renderTechniqueDropdown(selectId, selectedId = null) {
    const dropdown = document.getElementById(selectId);
    if (!dropdown) return;
    
    dropdown.innerHTML = '<option value="">请选择技巧</option>' + 
        state.techniques
            .filter(t => t.enabled)
            .map(technique => `<option value="${technique.id}" ${technique.id == selectedId ? 'selected' : ''}>
                ${technique.name} - ${technique.description || ''}
            </option>`).join('');
}

/**
 * 渲染情绪选择下拉框
 */
function renderEmotionDropdown(selectId, selectedId = null) {
    const dropdown = document.getElementById(selectId);
    if (!dropdown) return;
    
    dropdown.innerHTML = '<option value="">请选择情绪</option>' + 
        state.emotions
            .filter(e => e.enabled)
            .map(emotion => `<option value="${emotion.id}" ${emotion.id == selectedId ? 'selected' : ''}>
                ${emotion.name} - ${emotion.description || ''}
            </option>`).join('');
}

// ==================== 演唱配置管理 ====================

let singingConfigs = [];
let currentSingingConfigFilter = 'all';

async function loadSingingConfigs() {
    try {
        const response = await fetch(`${API_BASE}/singing-configs`);
        const data = await response.json();
        if (data.success) {
            singingConfigs = data.data || [];
        }
    } catch (error) {
        console.error('加载演唱配置失败:', error);
    }
}

async function renderSingingConfigs() {
    await loadSingingConfigs();
    renderSingingConfigsGrid();
}

function filterSingingConfigs(category) {
    currentSingingConfigFilter = category;
    
    // 更新按钮状态
    document.querySelectorAll('.config-filter-bar .filter-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.filter === category);
    });
    
    renderSingingConfigsGrid();
}

function renderSingingConfigsGrid() {
    const container = document.getElementById('singing-configs-grid');
    if (!container) return;
    
    let configs = singingConfigs;
    if (currentSingingConfigFilter !== 'all') {
        configs = singingConfigs.filter(c => c.category === currentSingingConfigFilter);
    }
    
    if (configs.length === 0) {
        container.innerHTML = '<p class="empty-hint">暂无演唱配置</p>';
        return;
    }
    
    container.innerHTML = configs.map(config => `
        <div class="singing-config-card ${config.isPreset ? 'preset' : ''}">
            <div class="config-card-header">
                <h4>
                    ${config.name}
                    ${config.isPreset ? '<span class="preset-badge">预设</span>' : ''}
                </h4>
                <span class="config-card-category">${config.category || '通用'}</span>
            </div>
            <div class="config-card-desc">${config.description || '暂无描述'}</div>
            <div class="config-card-params">
                <div class="config-param">
                    <span class="config-param-label">BPM</span>
                    <span class="config-param-value">${config.defaultBpm || 120}</span>
                </div>
                <div class="config-param">
                    <span class="config-param-label">颤音</span>
                    <span class="config-param-value">${config.vibratoDepth || 50}</span>
                </div>
                <div class="config-param">
                    <span class="config-param-label">气声</span>
                    <span class="config-param-value">${config.breathiness || 30}</span>
                </div>
                <div class="config-param">
                    <span class="config-param-label">混响</span>
                    <span class="config-param-value">${config.reverbAmount || 30}</span>
                </div>
            </div>
            <div class="config-card-actions">
                <button class="btn-use" onclick="useSingingConfig(${config.id})">使用此配置</button>
                <button class="btn-duplicate" onclick="duplicateSingingConfig(${config.id})">复制</button>
                ${!config.isPreset ? `<button class="btn-edit" onclick="editSingingConfig(${config.id})">编辑</button>` : ''}
            </div>
        </div>
    `).join('');
}

function useSingingConfig(id) {
    // 记录使用
    fetch(`${API_BASE}/singing-configs/${id}/use`, { method: 'POST' });
    showToast('配置已选择，可在创作向导中使用', 'success');
}

async function duplicateSingingConfig(id) {
    const config = singingConfigs.find(c => c.id === id);
    const newName = prompt('请输入新配置名称：', config ? config.name + ' (副本)' : '新配置');
    
    if (!newName) return;
    
    try {
        const response = await fetch(`${API_BASE}/singing-configs/${id}/duplicate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: newName })
        });
        
        const data = await response.json();
        if (data.success) {
            await loadSingingConfigs();
            renderSingingConfigsGrid();
            showToast('配置复制成功', 'success');
        }
    } catch (error) {
        showToast('复制失败', 'error');
    }
}

function editSingingConfig(id) {
    showToast('编辑功能开发中...', 'info');
}

function showAddSingingConfigModal() {
    document.getElementById('modal-singing-config-title').textContent = '添加演唱配置';
    document.getElementById('singing-config-id').value = '';
    document.getElementById('form-singing-config').reset();
    
    // 重置所有范围滑块的显示值
    document.querySelectorAll('#form-singing-config input[type="range"]').forEach(input => {
        const displayId = input.getAttribute('oninput')?.match(/updateRangeDisplay\(this, '([^']+)'\)/)?.[1];
        if (displayId) {
            document.getElementById(displayId).textContent = input.value;
        }
    });
    
    // 切换到第一个标签页
    switchSingingConfigTab('basic');
    
    openModal('modal-singing-config');
}

function editSingingConfigModal(id) {
    const config = singingConfigs.find(c => c.id === id);
    if (!config) {
        showToast('配置不存在', 'error');
        return;
    }
    
    document.getElementById('modal-singing-config-title').textContent = '编辑演唱配置';
    document.getElementById('singing-config-id').value = config.id;
    
    // 填充基本信息
    document.getElementById('sc-name').value = config.name || '';
    document.getElementById('sc-name-en').value = config.nameEn || '';
    document.getElementById('sc-category').value = config.category || '流行';
    document.getElementById('sc-enabled').value = config.enabled ? 'true' : 'false';
    document.getElementById('sc-description').value = config.description || '';
    document.getElementById('sc-use-case').value = config.useCase || '';
    
    // 填充节奏控制
    setInputAndDisplay('sc-default-bpm', config.defaultBpm || 120);
    document.getElementById('sc-time-signature').value = config.timeSignature || '4/4';
    setInputAndDisplay('sc-swing-feel', config.swingFeel || 20, 'sc-swing-val');
    setInputAndDisplay('sc-timing-offset', config.timingOffset || 0);
    document.getElementById('sc-auto-breath').value = config.autoBreath !== false ? 'true' : 'false';
    setInputAndDisplay('sc-breath-strength', config.breathStrength || 50, 'sc-breath-str-val');
    
    // 填充力度控制
    setInputAndDisplay('sc-base-volume', config.baseVolume || 70, 'sc-vol-val');
    setInputAndDisplay('sc-dynamics-min', config.dynamicsMin || 40, 'sc-dyn-min-val');
    setInputAndDisplay('sc-dynamics-max', config.dynamicsMax || 100, 'sc-dyn-max-val');
    setInputAndDisplay('sc-attack-speed', config.attackSpeed || 30, 'sc-attack-val');
    setInputAndDisplay('sc-release-speed', config.releaseSpeed || 40, 'sc-release-val');
    setInputAndDisplay('sc-accent-strength', config.accentStrength || 60, 'sc-accent-val');
    document.getElementById('sc-auto-dynamics').value = config.autoDynamics !== false ? 'true' : 'false';
    
    // 填充发音控制
    setInputAndDisplay('sc-articulation-clarity', config.articulationClarity || 70, 'sc-clarity-val');
    setInputAndDisplay('sc-legato-amount', config.legatoAmount || 60, 'sc-legato-val');
    setInputAndDisplay('sc-consonant-strength', config.consonantStrength || 50, 'sc-consonant-val');
    setInputAndDisplay('sc-vowel-length', config.vowelLength || 50, 'sc-vowel-val');
    document.getElementById('sc-ending-style').value = config.endingStyle || 'natural';
    document.getElementById('sc-pronunciation-style').value = config.pronunciationStyle || 'standard';
    
    // 填充音高控制
    setInputAndDisplay('sc-pitch-shift', config.pitchShift || 0);
    document.getElementById('sc-portamento-enabled').value = config.portamentoEnabled !== false ? 'true' : 'false';
    setInputAndDisplay('sc-portamento-time', config.portamentoTime || 80);
    setInputAndDisplay('sc-pitch-correction', config.pitchCorrection || 50, 'sc-pitch-corr-val');
    setInputAndDisplay('sc-pitch-drift', config.pitchDrift || 20, 'sc-pitch-drift-val');
    setInputAndDisplay('sc-portamento-range', config.portamentoRange || 2);
    
    // 填充颤音控制
    setInputAndDisplay('sc-vibrato-depth', config.vibratoDepth || 50, 'sc-vib-depth-val');
    setInputAndDisplay('sc-vibrato-rate', config.vibratoRate || 50, 'sc-vib-rate-val');
    setInputAndDisplay('sc-vibrato-delay', config.vibratoDelay || 200);
    setInputAndDisplay('sc-vibrato-attack', config.vibratoAttack || 100);
    document.getElementById('sc-auto-vibrato').value = config.autoVibrato !== false ? 'true' : 'false';
    setInputAndDisplay('sc-auto-vibrato-threshold', config.autoVibratoThreshold || 400);
    
    // 填充音色控制
    setInputAndDisplay('sc-breathiness', config.breathiness || 30, 'sc-breathiness-val');
    setInputAndDisplay('sc-tension', config.tension || 50, 'sc-tension-val');
    setInputAndDisplay('sc-brightness', config.brightness || 50, 'sc-brightness-val');
    setInputAndDisplay('sc-gender-factor', config.genderFactor || 50, 'sc-gender-val');
    document.getElementById('sc-resonance-type').value = config.resonanceType || 'mixed';
    setInputAndDisplay('sc-nasality', config.nasality || 30, 'sc-nasality-val');
    
    // 填充效果控制
    setInputAndDisplay('sc-reverb-amount', config.reverbAmount || 30, 'sc-reverb-val');
    document.getElementById('sc-reverb-type').value = config.reverbType || 'room';
    setInputAndDisplay('sc-delay-amount', config.delayAmount || 0, 'sc-delay-val');
    document.getElementById('sc-harmony-enabled').value = config.harmonyEnabled ? 'true' : 'false';
    document.getElementById('sc-harmony-type').value = config.harmonyType || 'third';
    setInputAndDisplay('sc-chorus-amount', config.chorusAmount || 0, 'sc-chorus-val');
    
    switchSingingConfigTab('basic');
    openModal('modal-singing-config');
}

function setInputAndDisplay(inputId, value, displayId = null) {
    const input = document.getElementById(inputId);
    if (input) {
        input.value = value;
    }
    if (displayId) {
        const display = document.getElementById(displayId);
        if (display) {
            display.textContent = value;
        }
    }
}

function switchSingingConfigTab(tabName) {
    // 移除所有active状态
    document.querySelectorAll('#modal-singing-config .form-tab').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('#modal-singing-config .form-tab-panel').forEach(panel => panel.classList.remove('active'));
    
    // 激活对应标签
    const tabIndex = ['basic', 'rhythm', 'dynamics', 'articulation', 'pitch', 'timbre', 'effects'].indexOf(tabName);
    if (tabIndex >= 0) {
        document.querySelectorAll('#modal-singing-config .form-tab')[tabIndex]?.classList.add('active');
        document.getElementById(`singing-config-tab-${tabName}`)?.classList.add('active');
    }
}

function updateRangeDisplay(input, displayId) {
    document.getElementById(displayId).textContent = input.value;
}

async function saveSingingConfigForm(event) {
    if (event) event.preventDefault();
    
    const id = document.getElementById('singing-config-id').value;
    const configData = {
        name: document.getElementById('sc-name').value,
        nameEn: document.getElementById('sc-name-en').value,
        category: document.getElementById('sc-category').value,
        enabled: document.getElementById('sc-enabled').value === 'true',
        description: document.getElementById('sc-description').value,
        useCase: document.getElementById('sc-use-case').value,
        // 节奏
        defaultBpm: parseInt(document.getElementById('sc-default-bpm').value) || 120,
        timeSignature: document.getElementById('sc-time-signature').value,
        swingFeel: parseInt(document.getElementById('sc-swing-feel').value) || 20,
        timingOffset: parseInt(document.getElementById('sc-timing-offset').value) || 0,
        autoBreath: document.getElementById('sc-auto-breath').value === 'true',
        breathStrength: parseInt(document.getElementById('sc-breath-strength').value) || 50,
        // 力度
        baseVolume: parseInt(document.getElementById('sc-base-volume').value) || 70,
        dynamicsMin: parseInt(document.getElementById('sc-dynamics-min').value) || 40,
        dynamicsMax: parseInt(document.getElementById('sc-dynamics-max').value) || 100,
        attackSpeed: parseInt(document.getElementById('sc-attack-speed').value) || 30,
        releaseSpeed: parseInt(document.getElementById('sc-release-speed').value) || 40,
        accentStrength: parseInt(document.getElementById('sc-accent-strength').value) || 60,
        autoDynamics: document.getElementById('sc-auto-dynamics').value === 'true',
        // 发音
        articulationClarity: parseInt(document.getElementById('sc-articulation-clarity').value) || 70,
        legatoAmount: parseInt(document.getElementById('sc-legato-amount').value) || 60,
        consonantStrength: parseInt(document.getElementById('sc-consonant-strength').value) || 50,
        vowelLength: parseInt(document.getElementById('sc-vowel-length').value) || 50,
        endingStyle: document.getElementById('sc-ending-style').value,
        pronunciationStyle: document.getElementById('sc-pronunciation-style').value,
        // 音高
        pitchShift: parseInt(document.getElementById('sc-pitch-shift').value) || 0,
        portamentoEnabled: document.getElementById('sc-portamento-enabled').value === 'true',
        portamentoTime: parseInt(document.getElementById('sc-portamento-time').value) || 80,
        portamentoRange: parseInt(document.getElementById('sc-portamento-range').value) || 2,
        pitchCorrection: parseInt(document.getElementById('sc-pitch-correction').value) || 50,
        pitchDrift: parseInt(document.getElementById('sc-pitch-drift').value) || 20,
        // 颤音
        vibratoDepth: parseInt(document.getElementById('sc-vibrato-depth').value) || 50,
        vibratoRate: parseInt(document.getElementById('sc-vibrato-rate').value) || 50,
        vibratoDelay: parseInt(document.getElementById('sc-vibrato-delay').value) || 200,
        vibratoAttack: parseInt(document.getElementById('sc-vibrato-attack').value) || 100,
        autoVibrato: document.getElementById('sc-auto-vibrato').value === 'true',
        autoVibratoThreshold: parseInt(document.getElementById('sc-auto-vibrato-threshold').value) || 400,
        // 音色
        breathiness: parseInt(document.getElementById('sc-breathiness').value) || 30,
        tension: parseInt(document.getElementById('sc-tension').value) || 50,
        brightness: parseInt(document.getElementById('sc-brightness').value) || 50,
        genderFactor: parseInt(document.getElementById('sc-gender-factor').value) || 50,
        resonanceType: document.getElementById('sc-resonance-type').value,
        nasality: parseInt(document.getElementById('sc-nasality').value) || 30,
        // 效果
        reverbAmount: parseInt(document.getElementById('sc-reverb-amount').value) || 30,
        reverbType: document.getElementById('sc-reverb-type').value,
        delayAmount: parseInt(document.getElementById('sc-delay-amount').value) || 0,
        harmonyEnabled: document.getElementById('sc-harmony-enabled').value === 'true',
        harmonyType: document.getElementById('sc-harmony-type').value,
        chorusAmount: parseInt(document.getElementById('sc-chorus-amount').value) || 0,
        isPreset: false
    };
    
    try {
        const url = id ? `${API_BASE}/singing-configs/${id}` : `${API_BASE}/singing-configs`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(configData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast(id ? '配置更新成功！' : '配置创建成功！', 'success');
            closeModal('modal-singing-config');
            loadSingingConfigs();
            renderSingingConfigsTable();
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存演唱配置失败:', error);
        showToast('保存失败', 'error');
    }
}

/**
 * 渲染演唱配置下拉框
 */
function renderSingingConfigDropdown(selectId, selectedId = null) {
    const dropdown = document.getElementById(selectId);
    if (!dropdown) return;
    
    dropdown.innerHTML = '<option value="">请选择演唱配置</option>' + 
        singingConfigs
            .filter(c => c.enabled)
            .map(config => {
                const badge = config.isPreset ? '📌' : '🔧';
                return `<option value="${config.id}" ${config.id == selectedId ? 'selected' : ''}>
                    ${badge} ${config.name} (${config.category || '通用'})
                </option>`;
            }).join('');
}

/**
 * 更新所有演唱配置下拉框
 */
function updateAllSingingConfigDropdowns() {
    renderSingingConfigDropdown('wizard-singing-config');
}

/**
 * 演唱配置选择变化
 */
function onSingingConfigChange(configId) {
    const config = singingConfigs.find(c => c.id == configId);
    // 保存到wizard状态
    if (state.wizard) {
        state.wizard.singingConfigId = configId ? parseInt(configId) : null;
    }
    renderSingingConfigPreview(config);
}

/**
 * 渲染演唱配置预览
 */
function renderSingingConfigPreview(config) {
    const container = document.getElementById('singing-config-preview');
    if (!container) return;
    
    if (!config) {
        container.innerHTML = '<p class="empty-hint">请选择一个演唱配置</p>';
        return;
    }
    
    container.innerHTML = `
        <div class="preview-row">
            <div class="preview-item"><label>BPM:</label><span>${config.defaultBpm || 120}</span></div>
            <div class="preview-item"><label>节拍:</label><span>${config.timeSignature || '4/4'}</span></div>
            <div class="preview-item"><label>颤音:</label><span>${config.vibratoDepth || 50}%</span></div>
            <div class="preview-item"><label>气声:</label><span>${config.breathiness || 30}%</span></div>
            <div class="preview-item"><label>张力:</label><span>${config.tension || 50}%</span></div>
            <div class="preview-item"><label>混响:</label><span>${config.reverbAmount || 30}%</span></div>
            <div class="preview-item"><label>连音:</label><span>${config.legatoAmount || 60}%</span></div>
        </div>
    `;
}

function showSingingConfigDetail() {
    const selectEl = document.getElementById('wizard-singing-config');
    if (selectEl && selectEl.value) {
        const config = singingConfigs.find(c => c.id == selectEl.value);
        if (config) {
            alert(`演唱配置详情：\n\n名称: ${config.name}\n分类: ${config.category}\n描述: ${config.description}\n\nBPM: ${config.defaultBpm}\n颤音深度: ${config.vibratoDepth}%\n气声程度: ${config.breathiness}%\n张力: ${config.tension}%\n明亮度: ${config.brightness}%\n混响: ${config.reverbAmount}%`);
        }
    } else {
        showToast('请先选择一个配置', 'warning');
    }
}

async function renderSettings() {
    await Promise.all([
        loadLlmConfigs(),
        loadJamendoConfig(),
        loadSynthesisProviders()
    ]);
}

async function refreshSettings() {
    showToast('正在刷新配置...', 'info');
    await renderSettings();
    showToast('配置已刷新', 'success');
}

// ==================== LLM配置管理 ====================

async function loadLlmConfigs() {
    // 首先尝试从本地存储加载
    const localConfigs = LocalStorageManager.loadLlmConfigs();
    if (localConfigs && localConfigs.length > 0) {
        console.log('使用本地存储的LLM配置');
        llmConfigs = localConfigs;
        renderLlmConfigList();
    }
    
    // 然后从服务器加载（用于同步和更新）
    try {
        const response = await fetch(`${API_BASE}/config/llm`);
        const data = await response.json();
        
        if (data.success && data.data) {
            // 只有在成功获取数据时才更新
            llmConfigs = data.data || [];
            // 保存到本地存储
            LocalStorageManager.saveLlmConfigs(llmConfigs);
            renderLlmConfigList();
        } else {
            console.error('加载LLM配置失败:', data.message);
            // 如果已有本地数据，保留本地数据
            if (llmConfigs.length === 0) {
                showToast('加载配置失败: ' + (data.message || '未知错误'), 'error');
                llmConfigs = [];
                renderLlmConfigList();
            } else {
                // 有本地数据时，静默失败，保留当前显示
                console.warn('服务器加载失败，使用本地存储的配置');
            }
        }
    } catch (error) {
        console.error('加载LLM配置失败:', error);
        // 如果已有本地数据，保留本地数据
        if (llmConfigs.length === 0) {
            showToast('加载配置失败，请检查网络连接', 'error');
            llmConfigs = [];
            renderLlmConfigList();
        } else {
            // 有本地数据时，静默失败，保留当前显示
            console.warn('服务器加载失败，使用本地存储的配置:', error);
        }
    }
}

function renderLlmConfigList() {
    const container = document.getElementById('llm-config-list');
    if (!container) return;
    
    const icons = { 'qwen': '🔮', 'openai': '🤖', 'gemini': '💎' };
    
    container.innerHTML = llmConfigs.map(config => {
        const hasApiKey = config.apiKey && config.apiKey.length > 0;
        const icon = icons[config.provider] || '🔧';
        
        return `
            <div class="llm-config-card ${config.isActive ? 'active' : ''}">
                <div class="llm-config-info">
                    <div class="llm-config-icon">${icon}</div>
                    <div class="llm-config-details">
                        <h4>
                            ${config.displayName || config.provider}
                            ${config.isActive ? '<span class="active-badge">当前使用</span>' : ''}
                        </h4>
                        <div class="llm-config-meta">
                            <span>模型: ${config.modelName || '-'}</span>
                            <span>温度: ${config.temperature || 0.8}</span>
                            <span class="llm-config-status ${hasApiKey ? 'configured' : 'not-configured'}">
                                ${hasApiKey ? '✅ 已配置密钥' : '⚠️ 未配置密钥'}
                            </span>
                        </div>
                    </div>
                </div>
                <div class="llm-config-actions">
                    <button class="btn-edit" onclick="editLlmConfig(${config.id})">✏️ 编辑</button>
                    <button class="btn-activate" onclick="activateLlmConfig(${config.id})" ${config.isActive ? 'disabled' : ''}>
                        ${config.isActive ? '✓ 已激活' : '🔄 激活'}
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function editLlmConfig(id) {
    const config = llmConfigs.find(c => c.id === id);
    if (!config) return;
    
    document.getElementById('llm-config-id').value = config.id;
    document.getElementById('llm-config-provider').value = config.provider;
    document.getElementById('llm-display-name').value = config.displayName || '';
    
    // 处理API密钥：如果已配置，显示占位符；否则留空
    const apiKeyInput = document.getElementById('llm-api-key');
    const hasApiKey = config.apiKey && config.apiKey.length > 0;
    if (hasApiKey) {
        // 显示占位符，表示密钥已配置（安全起见不显示真实密钥）
        apiKeyInput.value = '';
        apiKeyInput.placeholder = '•••••••••••• (已配置，留空不修改)';
        // 添加一个data属性标记，用于保存时判断
        apiKeyInput.setAttribute('data-has-key', 'true');
    } else {
        apiKeyInput.value = '';
        apiKeyInput.placeholder = '输入API密钥';
        apiKeyInput.removeAttribute('data-has-key');
    }
    
    document.getElementById('llm-api-url').value = config.apiUrl || '';
    document.getElementById('llm-model-name').value = config.modelName || '';
    document.getElementById('llm-temperature').value = config.temperature || 0.8;
    document.getElementById('llm-max-tokens').value = config.maxTokens || 2000;
    document.getElementById('llm-timeout').value = config.timeoutSeconds || 60;
    document.getElementById('llm-enabled').value = config.enabled ? 'true' : 'false';
    document.getElementById('llm-description').value = config.description || '';
    
    document.getElementById('modal-llm-config-title').textContent = `编辑 ${config.displayName || config.provider} 配置`;
    openModal('modal-llm-config');
}

async function saveLlmConfig(event) {
    event.preventDefault();
    
    const id = document.getElementById('llm-config-id').value;
    const apiKey = document.getElementById('llm-api-key').value;
    
    const configData = {
        displayName: document.getElementById('llm-display-name').value,
        apiUrl: document.getElementById('llm-api-url').value,
        modelName: document.getElementById('llm-model-name').value,
        temperature: parseFloat(document.getElementById('llm-temperature').value),
        maxTokens: parseInt(document.getElementById('llm-max-tokens').value),
        timeoutSeconds: parseInt(document.getElementById('llm-timeout').value),
        enabled: document.getElementById('llm-enabled').value === 'true',
        description: document.getElementById('llm-description').value
    };
    
    // 只有输入了新密钥才更新
    if (apiKey && apiKey.length > 0) {
        configData.apiKey = apiKey;
    }
    
    try {
        const response = await fetch(`${API_BASE}/config/llm/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(configData)
        });
        
        const data = await response.json();
        
        console.log('保存LLM配置响应:', data);
        
        if (data.success) {
            showToast('LLM配置已保存', 'success');
            // 更新本地数据，避免重新加载时数据丢失
            const updatedConfig = data.data;
            console.log('更新后的LLM配置数据:', updatedConfig);
            
            if (updatedConfig && updatedConfig.id) {
                const index = llmConfigs.findIndex(c => c.id === updatedConfig.id);
                if (index >= 0) {
                    // 合并更新，保留原有数据中可能缺失的字段
                    const existing = llmConfigs[index];
                    // 如果用户输入了新密钥，使用新密钥；否则保留原有密钥
                    const newApiKey = document.getElementById('llm-api-key').value;
                    const finalApiKey = (newApiKey && newApiKey.length > 0) 
                        ? newApiKey 
                        : (updatedConfig.apiKey || existing.apiKey);
                    
                    llmConfigs[index] = { 
                        ...existing, 
                        ...updatedConfig,
                        // 确保关键字段存在
                        id: updatedConfig.id,
                        provider: updatedConfig.provider || existing.provider,
                        displayName: updatedConfig.displayName || existing.displayName,
                        apiKey: finalApiKey, // 使用最终确定的密钥
                        apiUrl: updatedConfig.apiUrl || existing.apiUrl,
                        modelName: updatedConfig.modelName || existing.modelName
                    };
                    console.log('合并后的LLM配置数据 (apiKey已保留):', {
                        ...llmConfigs[index],
                        apiKey: llmConfigs[index].apiKey ? '***已配置***' : '未配置'
                    });
                } else {
                    llmConfigs.push(updatedConfig);
                }
                // 保存到本地存储
                LocalStorageManager.saveLlmConfigs(llmConfigs);
                // 立即重新渲染列表
                renderLlmConfigList();
            }
            closeModal('modal-llm-config');
            // 延迟重新加载以确保数据同步，但即使失败也保留当前数据
            setTimeout(async () => {
                try {
                    await loadLlmConfigs();
                } catch (error) {
                    console.error('重新加载LLM配置失败，但保留当前数据:', error);
                }
            }, 300);
        } else {
            showToast(data.message || '保存失败', 'error');
            console.error('保存LLM配置失败:', data);
        }
    } catch (error) {
        console.error('保存LLM配置失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

async function activateLlmConfig(id) {
    try {
        const response = await fetch(`${API_BASE}/config/llm/${id}/activate`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            // 更新本地配置的激活状态
            llmConfigs.forEach(c => c.isActive = (c.id == id));
            // 保存到本地存储
            LocalStorageManager.saveLlmConfigs(llmConfigs);
            await loadLlmConfigs();
            showToast(`已切换到 ${data.data.displayName}`, 'success');
        } else {
            showToast(data.message || '切换失败', 'error');
        }
    } catch (error) {
        console.error('切换LLM失败:', error);
        showToast('切换失败，请重试', 'error');
    }
}

// ==================== Jamendo配置管理 ====================

async function loadJamendoConfig() {
    try {
        const response = await fetch(`${API_BASE}/config/jamendo`);
        const data = await response.json();
        
        if (data.success) {
            jamendoConfig = data.data;
            fillJamendoForm(jamendoConfig);
        }
    } catch (error) {
        console.error('加载Jamendo配置失败:', error);
    }
}

function fillJamendoForm(config) {
    if (!config) return;
    
    document.getElementById('jamendo-enabled').value = config.enabled ? 'true' : 'false';
    document.getElementById('jamendo-client-id').value = config.clientId || '';
    document.getElementById('jamendo-api-url').value = config.apiUrl || 'https://api.jamendo.com/v3.0';
    document.getElementById('jamendo-audio-format').value = config.audioFormat || 'mp32';
    document.getElementById('jamendo-page-size').value = config.defaultPageSize || 20;
    document.getElementById('jamendo-max-results').value = config.maxResults || 100;
    document.getElementById('jamendo-commercial').value = config.commercialOnly ? 'true' : 'false';
    
    // 更新配置来源标识
    const badge = document.getElementById('jamendo-config-source');
    if (badge) {
        badge.textContent = config.id ? '数据库配置' : 'YAML兜底';
    }
}

async function saveJamendoConfig(event) {
    event.preventDefault();
    
    const configData = {
        name: 'default',
        enabled: document.getElementById('jamendo-enabled').value === 'true',
        clientId: document.getElementById('jamendo-client-id').value,
        apiUrl: document.getElementById('jamendo-api-url').value,
        audioFormat: document.getElementById('jamendo-audio-format').value,
        defaultPageSize: parseInt(document.getElementById('jamendo-page-size').value),
        maxResults: parseInt(document.getElementById('jamendo-max-results').value),
        commercialOnly: document.getElementById('jamendo-commercial').value === 'true'
    };
    
    try {
        const response = await fetch(`${API_BASE}/config/jamendo`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(configData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            jamendoConfig = data.data;
            fillJamendoForm(jamendoConfig);
            showToast('Jamendo配置已保存', 'success');
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存Jamendo配置失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

async function testJamendoConnection() {
    try {
        showToast('正在测试连接...', 'info');
        
        const response = await fetch(`${API_BASE}/config/jamendo/test`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success && data.data.success) {
            showToast('✅ 连接成功', 'success');
        } else {
            showToast('⚠️ ' + (data.data?.message || '连接失败'), 'warning');
        }
    } catch (error) {
        console.error('测试连接失败:', error);
        showToast('测试失败，请检查网络', 'error');
    }
}

// 保留旧函数兼容性
function updateSettingsLlmOptions() {
    renderLlmConfigList();
}

async function settingsSwitchLlm(provider) {
    const config = llmConfigs.find(c => c.provider === provider);
    if (config) {
        await activateLlmConfig(config.id);
    }
}

// ==================== 语音合成服务配置管理 ====================

let synthesisProviders = [];

async function loadSynthesisProviders() {
    // 首先尝试从本地存储加载
    const localProviders = LocalStorageManager.loadSynthesisProviders();
    if (localProviders && localProviders.length > 0) {
        console.log('使用本地存储的语音合成服务配置');
        synthesisProviders = localProviders;
        renderSynthesisProviderList();
    }
    
    // 然后从服务器加载（用于同步和更新）
    try {
        const response = await fetch(`${API_BASE}/synthesis-providers`);
        const data = await response.json();
        
        if (data.success && data.data) {
            // 只有在成功获取数据时才更新
            synthesisProviders = data.data || [];
            // 保存到本地存储
            LocalStorageManager.saveSynthesisProviders(synthesisProviders);
            renderSynthesisProviderList();
        } else {
            console.error('加载语音合成服务配置失败:', data.message);
            // 如果已有本地数据，保留本地数据
            if (synthesisProviders.length === 0) {
                showToast('加载配置失败: ' + (data.message || '未知错误'), 'error');
                synthesisProviders = [];
                renderSynthesisProviderList();
            } else {
                // 有本地数据时，静默失败，保留当前显示
                console.warn('服务器加载失败，使用本地存储的配置');
            }
        }
    } catch (error) {
        console.error('加载语音合成服务配置失败:', error);
        // 如果已有本地数据，保留本地数据
        if (synthesisProviders.length === 0) {
            showToast('加载配置失败，请检查网络连接', 'error');
            synthesisProviders = [];
            renderSynthesisProviderList();
        } else {
            // 有本地数据时，静默失败，保留当前显示
            console.warn('服务器加载失败，使用本地存储的配置:', error);
        }
    }
}

function renderSynthesisProviderList() {
    const container = document.getElementById('synthesis-provider-list');
    if (!container) return;
    
    // 如果没有数据，显示空状态
    if (!synthesisProviders || synthesisProviders.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🔧</div>
                <div class="empty-state-text">暂无语音合成服务配置</div>
                <div class="empty-state-hint">正在加载配置...</div>
            </div>
        `;
        return;
    }
    
    const typeIcons = {
        'cloud': '☁️',
        'local': '💻',
        'api': '🔌'
    };
    
    const serviceIcons = {
        'tts': '🗣️',
        'svs': '🎤',
        'vc': '🔄'
    };
    
    // 按类型分组
    const cloudProviders = synthesisProviders.filter(p => p.providerType === 'cloud');
    const localProviders = synthesisProviders.filter(p => p.providerType === 'local');
    const apiProviders = synthesisProviders.filter(p => p.providerType === 'api');
    
    let html = '';
    
    // 云端服务
    if (cloudProviders.length > 0) {
        html += `
            <div class="provider-section">
                <h4>☁️ 云端服务</h4>
                <div class="provider-grid">
                    ${cloudProviders.map(provider => renderProviderCard(provider, serviceIcons)).join('')}
                </div>
            </div>
        `;
    }
    
    // API服务
    if (apiProviders.length > 0) {
        html += `
            <div class="provider-section">
                <h4>🔌 API服务</h4>
                <div class="provider-grid">
                    ${apiProviders.map(provider => renderProviderCard(provider, serviceIcons)).join('')}
                </div>
            </div>
        `;
    }
    
    // 本地部署
    if (localProviders.length > 0) {
        html += `
            <div class="provider-section">
                <h4>💻 本地部署</h4>
                <div class="provider-grid">
                    ${localProviders.map(provider => renderProviderCard(provider, serviceIcons)).join('')}
                </div>
            </div>
        `;
    }
    
    // 如果所有分组都为空，显示空状态
    if (!html) {
        html = `
            <div class="empty-state">
                <div class="empty-state-icon">🔧</div>
                <div class="empty-state-text">暂无语音合成服务配置</div>
                <div class="empty-state-hint">请联系管理员配置服务</div>
            </div>
        `;
    }
    
    // 确保容器始终有内容，避免元素消失
    if (container) {
        container.innerHTML = html;
    } else {
        console.error('synthesis-provider-list 容器不存在');
    }
}

function renderProviderCard(provider, serviceIcons) {
    // 防御性检查，确保provider对象存在且有效
    if (!provider || !provider.id) {
        console.error('无效的provider数据:', provider);
        return '';
    }
    
    const hasApiKey = provider.apiKey && provider.apiKey.length > 0;
    const statusClass = provider.enabled && hasApiKey ? 'configured' : 'pending';
    const statusText = provider.enabled && hasApiKey ? '已配置' : '待配置';
    const activeClass = provider.isActive ? 'active' : '';
    const serviceIcon = serviceIcons[provider.serviceType] || '🔧';
    const displayName = provider.displayName || provider.provider || '未知服务';
    const description = provider.description ? provider.description.substring(0, 50) + '...' : '';
    
    return `
        <div class="provider-card ${statusClass} ${activeClass}" onclick="editSynthesisProvider(${provider.id})">
            <div class="provider-card-header">
                <span class="provider-name">${displayName}</span>
                <span class="provider-service-type">${serviceIcon}</span>
            </div>
            <div class="provider-card-status">
                <span class="status-badge ${statusClass}">${statusText}</span>
                ${provider.isActive ? '<span class="active-badge">✓ 使用中</span>' : ''}
            </div>
            <div class="provider-card-info">
                ${description ? `<small>${description}</small>` : ''}
            </div>
            <div class="provider-card-actions">
                <button class="btn-link" onclick="event.stopPropagation(); editSynthesisProvider(${provider.id})">配置</button>
                ${provider.enabled && hasApiKey ? 
                    `<button class="btn-link" onclick="event.stopPropagation(); switchSynthesisProvider('${provider.provider}')">
                        ${provider.isActive ? '使用中' : '设为默认'}
                    </button>` : ''}
            </div>
        </div>
    `;
}

function editSynthesisProvider(id) {
    const provider = synthesisProviders.find(p => p.id === id);
    if (!provider) {
        showToast('配置不存在', 'error');
        return;
    }
    
    document.getElementById('modal-synthesis-provider-title').textContent = `配置 ${provider.displayName}`;
    document.getElementById('sp-id').value = provider.id;
    document.getElementById('sp-provider').value = provider.provider;
    document.getElementById('sp-display-name').value = provider.displayName;
    document.getElementById('sp-enabled').value = provider.enabled ? 'true' : 'false';
    document.getElementById('sp-api-key').value = provider.apiKey || '';
    document.getElementById('sp-api-key-secondary').value = provider.apiKeySecondary || '';
    document.getElementById('sp-api-url').value = provider.apiUrl || '';
    document.getElementById('sp-region').value = provider.region || '';
    document.getElementById('sp-timeout').value = provider.timeoutSeconds || 120;
    document.getElementById('sp-max-concurrent').value = provider.maxConcurrent || 5;
    document.getElementById('sp-rate-limit').value = provider.rateLimit || 60;
    
    // 显示/隐藏特定字段
    const needsSecondaryKey = ['azure-speech', 'xunfei', 'baidu-tts'].includes(provider.provider);
    document.getElementById('sp-api-key-secondary-group').style.display = needsSecondaryKey ? 'block' : 'none';
    
    const needsRegion = ['azure-speech', 'google-tts', 'amazon-polly', 'tencent-tts'].includes(provider.provider);
    document.getElementById('sp-region-group').style.display = needsRegion ? 'block' : 'none';
    
    // 更新提供商信息
    const infoBox = document.getElementById('sp-info-box');
    infoBox.innerHTML = `
        <div class="info-item"><strong>服务类型:</strong> ${provider.serviceType === 'tts' ? '文字转语音' : (provider.serviceType === 'svs' ? '歌声合成' : '声音转换')}</div>
        <div class="info-item"><strong>部署类型:</strong> ${provider.providerType === 'cloud' ? '云端服务' : '本地部署'}</div>
        ${provider.pricingInfo ? `<div class="info-item"><strong>价格:</strong> ${provider.pricingInfo}</div>` : ''}
        ${provider.websiteUrl ? `<div class="info-item"><a href="${provider.websiteUrl}" target="_blank">🔗 官网</a> | <a href="${provider.docsUrl}" target="_blank">📚 文档</a></div>` : ''}
    `;
    
    openModal('modal-synthesis-provider');
}

function showAddSynthesisProviderModal() {
    showToast('暂不支持添加自定义服务，请使用预置服务', 'info');
}

async function saveSynthesisProvider(event) {
    if (event) event.preventDefault();
    
    const id = document.getElementById('sp-id').value;
    const configData = {
        provider: document.getElementById('sp-provider').value,
        displayName: document.getElementById('sp-display-name').value,
        enabled: document.getElementById('sp-enabled').value === 'true',
        apiKey: document.getElementById('sp-api-key').value,
        apiKeySecondary: document.getElementById('sp-api-key-secondary').value,
        apiUrl: document.getElementById('sp-api-url').value,
        region: document.getElementById('sp-region').value,
        timeoutSeconds: parseInt(document.getElementById('sp-timeout').value) || 120,
        maxConcurrent: parseInt(document.getElementById('sp-max-concurrent').value) || 5,
        rateLimit: parseInt(document.getElementById('sp-rate-limit').value) || 60
    };
    
    try {
        const response = await fetch(`${API_BASE}/synthesis-providers/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(configData)
        });
        
        const data = await response.json();
        
        console.log('保存配置响应:', data);
        
        if (data.success) {
            showToast('配置已保存', 'success');
            // 更新本地数据，避免重新加载时数据丢失
            const updatedProvider = data.data;
            console.log('更新后的provider数据:', updatedProvider);
            console.log('当前synthesisProviders数量:', synthesisProviders.length);
            
            if (updatedProvider && updatedProvider.id) {
                const index = synthesisProviders.findIndex(p => p.id === updatedProvider.id);
                console.log('找到的索引:', index);
                
                if (index >= 0) {
                    // 合并更新，保留原有数据中可能缺失的字段（如description等）
                    const existing = synthesisProviders[index];
                    synthesisProviders[index] = { 
                        ...existing, 
                        ...updatedProvider,
                        // 确保关键字段存在
                        id: updatedProvider.id,
                        provider: updatedProvider.provider || existing.provider,
                        displayName: updatedProvider.displayName || existing.displayName,
                        providerType: updatedProvider.providerType || existing.providerType,
                        serviceType: updatedProvider.serviceType || existing.serviceType,
                        description: updatedProvider.description || existing.description
                    };
                    console.log('合并后的数据:', synthesisProviders[index]);
                } else {
                    // 如果找不到，添加到列表
                    console.log('未找到现有配置，添加新配置');
                    synthesisProviders.push(updatedProvider);
                }
                // 保存到本地存储
                LocalStorageManager.saveSynthesisProviders(synthesisProviders);
                // 立即重新渲染列表
                console.log('重新渲染列表，当前数据量:', synthesisProviders.length);
                renderSynthesisProviderList();
            } else {
                console.warn('保存成功但返回数据格式异常:', data);
                // 即使数据格式异常，也尝试重新加载
            }
            closeModal('modal-synthesis-provider');
            // 延迟重新加载以确保数据同步，但即使失败也保留当前数据
            setTimeout(async () => {
                try {
                    console.log('开始重新加载配置...');
                    await loadSynthesisProviders();
                    console.log('重新加载完成，当前数据量:', synthesisProviders.length);
                } catch (error) {
                    console.error('重新加载配置失败，但保留当前数据:', error);
                    // 不显示错误提示，因为数据已经更新了
                }
            }, 300);
        } else {
            showToast(data.message || '保存失败', 'error');
            console.error('保存配置失败:', data);
        }
    } catch (error) {
        console.error('保存语音合成配置失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

async function switchSynthesisProvider(provider) {
    try {
        const response = await fetch(`${API_BASE}/synthesis-providers/switch/${provider}`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('已切换默认服务', 'success');
            await loadSynthesisProviders();
        } else {
            showToast(data.message || '切换失败', 'error');
        }
    } catch (error) {
        console.error('切换语音合成服务失败:', error);
        showToast('切换失败', 'error');
    }
}

async function testSynthesisProvider() {
    const id = document.getElementById('sp-id').value;
    
    try {
        showToast('正在测试连接...', 'info');
        
        const response = await fetch(`${API_BASE}/synthesis-providers/${id}/test`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('✅ 连接测试完成', 'success');
        } else {
            showToast('⚠️ ' + (data.message || '测试失败'), 'warning');
        }
    } catch (error) {
        console.error('测试连接失败:', error);
        showToast('测试失败，请检查配置', 'error');
    }
}

// ========================================
// 工具函数
// ========================================

function showLoading(show, text = '加载中...') {
    const overlay = document.getElementById('loading-overlay');
    const loadingText = document.getElementById('loading-text');
    if (loadingText) loadingText.textContent = text;
    overlay.classList.toggle('hidden', !show);
}

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ========================================
// 模板库
// ========================================

function initTemplateFilters() {
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const category = btn.dataset.category;
            renderTemplates(category);
        });
    });
}

function renderTemplates(category = 'all') {
    const container = document.getElementById('templates-grid');
    if (!container) return;
    
    let filteredTemplates = state.templates;
    if (category !== 'all') {
        filteredTemplates = state.templates.filter(t => t.category === category);
    }
    
    if (filteredTemplates.length === 0) {
        container.innerHTML = '<p class="empty-hint">暂无模板</p>';
        return;
    }
    
    container.innerHTML = filteredTemplates.map(template => `
        <div class="template-card" onclick="useTemplate(${template.id})">
            <div class="template-header">
                <div class="template-icon">${template.iconEmoji || '📝'}</div>
                <div class="template-info">
                    <div class="template-name">${template.name}</div>
                    <span class="template-category">${template.category}</span>
                </div>
            </div>
            <div class="template-desc">${template.description || ''}</div>
            <div class="template-meta">
                <div class="template-meta-item">
                    <span>🎵</span>
                    <span>${template.suggestedBpm || 120} BPM</span>
                </div>
                <div class="template-meta-item">
                    <span>🎹</span>
                    <span>${template.suggestedKey || 'C大调'}</span>
                </div>
            </div>
            <div class="template-keywords">
                ${(template.moodKeywords || '').split(',').slice(0, 4).map(kw => 
                    `<span class="keyword-tag">${kw.trim()}</span>`
                ).join('')}
            </div>
            <div class="template-actions">
                <span class="template-use-count">已使用 ${template.useCount || 0} 次</span>
                <button class="btn-primary btn-sm" onclick="event.stopPropagation(); useTemplate(${template.id})">
                    使用模板
                </button>
            </div>
        </div>
    `).join('');
}

async function useTemplate(templateId) {
    const template = state.templates.find(t => t.id === templateId);
    if (!template) return;
    
    // 记录使用
    try {
        await fetch(`${API_BASE}/templates/${templateId}/use`, { method: 'POST' });
    } catch (e) {}
    
    // 跳转到向导并预填充模板数据
    state.wizard.selectedTemplate = template;
    navigateTo('wizard');
    
    // 预填充AI生成表单
    setTimeout(() => {
        const themeInput = document.getElementById('wizard-theme');
        const moodSelect = document.getElementById('wizard-mood');
        const styleSelect = document.getElementById('wizard-style');
        const keywordsInput = document.getElementById('wizard-keywords');
        
        if (themeInput && template.moodKeywords) {
            const keywords = template.moodKeywords.split(',');
            themeInput.value = keywords[0] || '';
        }
        if (styleSelect && template.category) {
            styleSelect.value = template.category;
        }
        if (keywordsInput && template.moodKeywords) {
            keywordsInput.value = template.moodKeywords;
        }
        
        showToast(`已加载模板: ${template.name}`, 'success');
    }, 300);
}

// ========================================
// 项目管理
// ========================================

function renderProjects() {
    const container = document.getElementById('projects-list');
    if (!container) return;
    
    if (state.projects.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div style="font-size: 64px; margin-bottom: 20px;">📁</div>
                <h3>暂无项目</h3>
                <p style="color: var(--text-secondary); margin-bottom: 20px;">开始创建您的第一个AI歌曲项目</p>
                <button class="btn-primary" onclick="navigateTo('wizard')">
                    ✨ 创建新项目
                </button>
            </div>
        `;
        return;
    }
    
    container.innerHTML = state.projects.map(project => `
        <div class="project-card">
            <div class="project-icon">🎵</div>
            <div class="project-info">
                <div class="project-name">${project.name}</div>
                <div class="project-meta">
                    <span>🎙️ ${project.singer?.name || '未选择歌手'}</span>
                    <span>📅 ${formatDate(project.updatedAt)}</span>
                </div>
            </div>
            <span class="project-status ${project.status || 'draft'}">
                ${project.status === 'completed' ? '已完成' : '草稿'}
            </span>
            <div class="project-actions">
                <button class="btn-icon" onclick="openProject(${project.id})" title="打开">📂</button>
                <button class="btn-icon" onclick="exportProject(${project.id})" title="导出">📤</button>
                <button class="btn-icon danger" onclick="deleteProject(${project.id})" title="删除">🗑️</button>
            </div>
        </div>
    `).join('');
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN');
}

async function createNewProject() {
    const name = prompt('请输入项目名称：');
    if (!name) return;
    
    try {
        const response = await fetch(`${API_BASE}/projects`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description: '' })
        });
        const data = await response.json();
        if (data.success) {
            state.projects.unshift(data.data);
            renderProjects();
            showToast('项目创建成功', 'success');
        }
    } catch (error) {
        showToast('创建失败', 'error');
    }
}

function openProject(projectId) {
    showToast('正在打开项目...', 'info');
    // TODO: 加载项目配置并跳转到编辑器
}

async function exportProject(projectId) {
    try {
        const response = await fetch(`${API_BASE}/projects/${projectId}/export`, { method: 'POST' });
        const data = await response.json();
        if (data.success) {
            // 下载JSON文件
            const blob = new Blob([JSON.stringify(data.data, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `project_${projectId}.json`;
            a.click();
            URL.revokeObjectURL(url);
            showToast('导出成功', 'success');
        }
    } catch (error) {
        showToast('导出失败', 'error');
    }
}

async function deleteProject(projectId) {
    if (!confirm('确定要删除这个项目吗？')) return;
    
    try {
        await fetch(`${API_BASE}/projects/${projectId}`, { method: 'DELETE' });
        state.projects = state.projects.filter(p => p.id !== projectId);
        renderProjects();
        showToast('项目已删除', 'success');
    } catch (error) {
        showToast('删除失败', 'error');
    }
}

// ========================================
// 占位函数
// ========================================

function showAddSingerModal() { showToast('功能开发中...', 'info'); }
function showAddTechniqueModal() { showToast('功能开发中...', 'info'); }
function showAddEmotionModal() { showToast('功能开发中...', 'info'); }
function editSinger(id) { showToast('功能开发中...', 'info'); }
function deleteSinger(id) { showToast('功能开发中...', 'info'); }
function editTechnique(id) { showToast('功能开发中...', 'info'); }
function editEmotion(id) { showToast('功能开发中...', 'info'); }
function editSongSegments(id) { showToast('功能开发中...', 'info'); }
function viewSongInWizard(id) { navigateTo('wizard'); }

// ========================================
// Jamendo 音乐库
// ========================================

async function initJamendo() {
    // 检查服务状态
    checkJamendoStatus();
    
    // 加载热门歌曲
    loadJamendoTracks('popular');
}

async function checkJamendoStatus() {
    try {
        const response = await fetch(`${API_BASE}/jamendo/status`);
        const data = await response.json();
        
        const statusEl = document.getElementById('jamendo-status');
        if (statusEl && data.data) {
            const indicator = statusEl.querySelector('.status-indicator');
            const text = statusEl.querySelector('.status-text');
            
            if (data.data.enabled && data.data.clientIdConfigured) {
                indicator.classList.add('online');
                indicator.classList.remove('offline');
                text.textContent = 'Jamendo服务已连接';
            } else if (data.data.enabled && !data.data.clientIdConfigured) {
                indicator.classList.remove('online', 'offline');
                text.textContent = '请配置JAMENDO_CLIENT_ID环境变量';
            } else {
                indicator.classList.add('offline');
                indicator.classList.remove('online');
                text.textContent = 'Jamendo服务未启用';
            }
        }
    } catch (error) {
        console.error('检查Jamendo状态失败:', error);
    }
}

async function loadJamendoTracks(type = 'search') {
    const container = document.getElementById('jamendo-tracks');
    container.innerHTML = `
        <div class="loading-placeholder">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
        </div>
    `;
    
    try {
        let url = `${API_BASE}/jamendo/`;
        const params = new URLSearchParams();
        params.append('limit', state.jamendo.pageSize);
        params.append('offset', (state.jamendo.currentPage - 1) * state.jamendo.pageSize);
        
        if (type === 'popular') {
            url += 'popular';
        } else if (type === 'latest') {
            url += 'latest';
        } else if (type === 'genre' && state.jamendo.filters.genre) {
            url += `genre/${state.jamendo.filters.genre}`;
        } else if (type === 'mood' && state.jamendo.filters.mood) {
            url += `mood/${state.jamendo.filters.mood}`;
        } else {
            url += 'search';
            if (state.jamendo.searchQuery) {
                params.append('search', state.jamendo.searchQuery);
            }
            if (state.jamendo.filters.genre) {
                params.append('tags', state.jamendo.filters.genre);
            }
            if (state.jamendo.filters.mood) {
                params.append('mood', state.jamendo.filters.mood);
            }
            if (state.jamendo.filters.vocal) {
                params.append('vocalInstrumental', state.jamendo.filters.vocal);
            }
            if (state.jamendo.filters.speed) {
                params.append('speed', state.jamendo.filters.speed);
            }
            if (state.jamendo.filters.order) {
                params.append('orderBy', state.jamendo.filters.order);
            }
        }
        
        // 请求包含歌词数据
        params.append('includeLyrics', 'true');
        
        url += '?' + params.toString();
        
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.success && data.data) {
            // 规范化字段名，确保兼容snake_case和camelCase
            state.jamendo.tracks = data.data.map(track => ({
                ...track,
                artistName: track.artistName || track.artist_name || '未知艺术家',
                albumName: track.albumName || track.album_name || null,
                name: track.name || track.title || '未知歌曲'
            }));
            renderJamendoTracks();
            updateJamendoPagination();
        } else {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🎵</div>
                    <div class="empty-state-text">未找到歌曲</div>
                    <div class="empty-state-hint">请尝试其他搜索条件或检查Jamendo配置</div>
                </div>
            `;
        }
    } catch (error) {
        console.error('加载Jamendo歌曲失败:', error);
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <div class="empty-state-text">加载失败</div>
                <div class="empty-state-hint">请检查网络连接和Jamendo配置</div>
            </div>
        `;
    }
}

function renderJamendoTracks() {
    const container = document.getElementById('jamendo-tracks');
    
    if (state.jamendo.tracks.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🔍</div>
                <div class="empty-state-text">未找到歌曲</div>
                <div class="empty-state-hint">尝试更改搜索条件</div>
            </div>
        `;
        return;
    }
    
    const isCurrentTrack = (trackId) => state.jamendo.currentTrack?.id === trackId;
    const isPlaying = (trackId) => isCurrentTrack(trackId) && state.jamendo.isPlaying;
    
    container.innerHTML = state.jamendo.tracks.map(track => {
        const isActive = isCurrentTrack(track.id);
        const playing = isPlaying(track.id);
        const progress = isActive && state.jamendo.duration > 0 
            ? (state.jamendo.currentTime / state.jamendo.duration) * 100 
            : 0;
        
        return `
        <div class="jamendo-track-list ${isActive ? 'track-playing' : ''}" data-track-id="${track.id}">
            <div class="track-list-main">
                <div class="track-list-thumb">
                    ${track.image ? 
                        `<img src="${track.image}" alt="${track.name || '未知歌曲'}" loading="lazy">` : 
                        `<div class="track-thumb-placeholder">🎵</div>`
                    }
                    <button class="track-list-play-btn" onclick="playJamendoTrack('${track.id}')">
                        ${playing ? '⏸' : '▶'}
                    </button>
                </div>
                <div class="track-list-info">
                    <div class="track-list-title-row">
                        <div class="track-list-title" title="${track.name || '未知歌曲'}">
                            ${track.name || '未知歌曲'}
                            ${playing ? '<span class="playing-indicator">●</span>' : ''}
                        </div>
                        <div class="track-list-duration">${formatDuration(track.duration)}</div>
                    </div>
                    <div class="track-list-meta">
                        <span class="track-list-artist" title="${track.artistName || '未知艺术家'}">
                            ${track.artistName || '未知艺术家'}
                        </span>
                        ${track.albumName ? `<span class="track-list-separator">•</span><span class="track-list-album">${track.albumName}</span>` : ''}
                        ${track.musicinfo?.tags?.genres?.length ? `
                            <span class="track-list-separator">•</span>
                            <span class="track-list-genre">${track.musicinfo.tags.genres[0]}</span>
                        ` : ''}
                    </div>
                    ${isActive ? `
                        <div class="track-progress-container">
                            <div class="track-progress-bar">
                                <div class="track-progress-fill" style="width: ${progress}%"></div>
                            </div>
                            <div class="track-progress-time">
                                <span>${formatDuration(state.jamendo.currentTime)}</span>
                                <span>/</span>
                                <span>${formatDuration(state.jamendo.duration || track.duration)}</span>
                            </div>
                        </div>
                    ` : ''}
                    ${isActive && track.lyrics ? `
                        <div class="track-lyrics-container">
                            <div class="track-lyrics-toggle" onclick="toggleTrackLyrics('${track.id}')">
                                <span>📝 查看歌词</span>
                            </div>
                            <div class="track-lyrics-content" id="lyrics-${track.id}" style="display: none;">
                                <pre>${track.lyrics}</pre>
                            </div>
                        </div>
                    ` : ''}
                </div>
                <div class="track-list-actions">
                    <button class="track-action-btn-sm" onclick="importJamendoTrack('${track.id}')" title="导入">
                        📥
                    </button>
                </div>
            </div>
        </div>
        `;
    }).join('');
}

function formatDuration(seconds) {
    if (!seconds || isNaN(seconds)) return '--:--';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
}

// 更新播放进度
function updateTrackProgress() {
    if (state.jamendo.audioElement && state.jamendo.isPlaying) {
        const progressBar = document.querySelector('.track-progress-fill');
        if (progressBar) {
            const progress = (state.jamendo.currentTime / state.jamendo.duration) * 100;
            progressBar.style.width = `${progress}%`;
        }
        const timeDisplay = document.querySelector('.track-progress-time');
        if (timeDisplay) {
            timeDisplay.innerHTML = `
                <span>${formatDuration(state.jamendo.currentTime)}</span>
                <span>/</span>
                <span>${formatDuration(state.jamendo.duration)}</span>
            `;
        }
    }
}

// 开始进度更新
function startProgressUpdate() {
    stopProgressUpdate(); // 先清除旧的
    state.jamendo.progressInterval = setInterval(() => {
        if (state.jamendo.audioElement && state.jamendo.isPlaying) {
            state.jamendo.currentTime = state.jamendo.audioElement.currentTime;
            updateTrackProgress();
        }
    }, 100);
}

// 停止进度更新
function stopProgressUpdate() {
    if (state.jamendo.progressInterval) {
        clearInterval(state.jamendo.progressInterval);
        state.jamendo.progressInterval = null;
    }
}

// 切换歌词显示
function toggleTrackLyrics(trackId) {
    const lyricsContent = document.getElementById(`lyrics-${trackId}`);
    const toggleBtn = lyricsContent?.previousElementSibling;
    if (lyricsContent) {
        const isVisible = lyricsContent.style.display !== 'none';
        lyricsContent.style.display = isVisible ? 'none' : 'block';
        if (toggleBtn) {
            toggleBtn.innerHTML = isVisible ? '<span>📝 查看歌词</span>' : '<span>📝 隐藏歌词</span>';
        }
    }
}

// 更新歌曲管理页面的播放进度
function updateSongsTrackProgress() {
    if (state.jamendo.audioElement && state.jamendo.isPlaying) {
        const progressBar = document.querySelector('.track-progress-fill-compact');
        if (progressBar) {
            const progress = (state.jamendo.currentTime / state.jamendo.duration) * 100;
            progressBar.style.width = `${progress}%`;
        }
        const timeDisplay = document.querySelector('.track-progress-time-compact');
        if (timeDisplay) {
            timeDisplay.innerHTML = `
                <span>${formatDuration(state.jamendo.currentTime)}</span>
                <span>/</span>
                <span>${formatDuration(state.jamendo.duration)}</span>
            `;
        }
    }
}

// 开始歌曲管理页面的进度更新
function startSongsProgressUpdate() {
    stopSongsProgressUpdate(); // 先清除旧的
    if (!state.jamendo.progressInterval) {
        state.jamendo.progressInterval = setInterval(() => {
            if (state.jamendo.audioElement && state.jamendo.isPlaying) {
                state.jamendo.currentTime = state.jamendo.audioElement.currentTime;
                updateSongsTrackProgress();
                // 同时更新主列表的进度
                updateTrackProgress();
            }
        }, 100);
    }
}

// 停止歌曲管理页面的进度更新
function stopSongsProgressUpdate() {
    // 注意：这里不清理interval，因为可能主列表也在使用
    // 实际的清理在stopProgressUpdate中统一处理
}

function updateJamendoPagination() {
    const prevBtn = document.getElementById('jamendo-prev');
    const nextBtn = document.getElementById('jamendo-next');
    const pageInfo = document.getElementById('jamendo-page-info');
    
    if (prevBtn) prevBtn.disabled = state.jamendo.currentPage <= 1;
    if (nextBtn) nextBtn.disabled = state.jamendo.tracks.length < state.jamendo.pageSize;
    if (pageInfo) pageInfo.textContent = `第 ${state.jamendo.currentPage} 页`;
}

function jamendoPage(delta) {
    state.jamendo.currentPage = Math.max(1, state.jamendo.currentPage + delta);
    loadJamendoTracks('search');
}

function searchJamendo() {
    const searchInput = document.getElementById('jamendo-search');
    state.jamendo.searchQuery = searchInput?.value?.trim() || '';
    state.jamendo.currentPage = 1;
    loadJamendoTracks('search');
}

function filterJamendo() {
    state.jamendo.filters.genre = document.getElementById('jamendo-genre')?.value || '';
    state.jamendo.filters.mood = document.getElementById('jamendo-mood')?.value || '';
    state.jamendo.filters.vocal = document.getElementById('jamendo-vocal')?.value || '';
    state.jamendo.filters.speed = document.getElementById('jamendo-speed')?.value || '';
    state.jamendo.filters.order = document.getElementById('jamendo-order')?.value || 'relevance';
    state.jamendo.currentPage = 1;
    loadJamendoTracks('search');
}

function quickSearchJamendo(type) {
    // 重置筛选
    state.jamendo.searchQuery = '';
    state.jamendo.currentPage = 1;
    state.jamendo.filters = {
        genre: '',
        mood: '',
        vocal: '',
        speed: '',
        order: 'relevance'
    };
    
    // 重置UI
    document.getElementById('jamendo-search').value = '';
    document.getElementById('jamendo-genre').value = '';
    document.getElementById('jamendo-mood').value = '';
    document.getElementById('jamendo-vocal').value = '';
    document.getElementById('jamendo-speed').value = '';
    document.getElementById('jamendo-order').value = 'relevance';
    
    if (type === 'popular') {
        loadJamendoTracks('popular');
    } else if (type === 'latest') {
        loadJamendoTracks('latest');
    } else {
        // 按流派加载
        state.jamendo.filters.genre = type;
        document.getElementById('jamendo-genre').value = type;
        loadJamendoTracks('genre');
    }
}

// 播放Jamendo歌曲
function playJamendoTrack(trackId) {
    const track = state.jamendo.tracks.find(t => t.id === trackId);
    if (!track) return;
    
    // 如果点击的是当前正在播放的歌曲
    if (state.jamendo.currentTrack?.id === trackId) {
        if (state.jamendo.isPlaying) {
            // 暂停播放
            if (state.jamendo.audioElement) {
                state.jamendo.audioElement.pause();
                state.jamendo.isPlaying = false;
            }
        } else {
            // 恢复播放
            if (state.jamendo.audioElement) {
                state.jamendo.audioElement.play().then(() => {
                    state.jamendo.isPlaying = true;
                    renderJamendoTracks();
                }).catch(err => {
                    console.error('恢复播放失败:', err);
                    showToast('恢复播放失败', 'error');
                });
            }
        }
        renderJamendoTracks();
        return;
    }
    
    // 播放新曲目
    // 先停止当前播放
    if (state.jamendo.audioElement) {
        state.jamendo.audioElement.pause();
        state.jamendo.audioElement = null;
    }
    
    state.jamendo.currentTrack = track;
    state.jamendo.audioElement = new Audio(track.audio);
    state.jamendo.audioElement.volume = 0.7;
    
    // 添加事件监听
    state.jamendo.audioElement.onloadedmetadata = () => {
        state.jamendo.duration = state.jamendo.audioElement.duration;
        renderJamendoTracks();
    };
    
    state.jamendo.audioElement.ontimeupdate = () => {
        state.jamendo.currentTime = state.jamendo.audioElement.currentTime;
        updateTrackProgress();
    };
    
    state.jamendo.audioElement.onplay = () => {
        state.jamendo.isPlaying = true;
        startProgressUpdate();
        renderJamendoTracks();
    };
    
    state.jamendo.audioElement.onpause = () => {
        state.jamendo.isPlaying = false;
        stopProgressUpdate();
        renderJamendoTracks();
    };
    
    state.jamendo.audioElement.onended = () => {
        state.jamendo.isPlaying = false;
        state.jamendo.currentTime = 0;
        state.jamendo.duration = 0;
        state.jamendo.currentTrack = null;
        stopProgressUpdate();
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement = null;
        }
        renderJamendoTracks();
        hideAudioPlayer();
    };
    
    state.jamendo.audioElement.onerror = () => {
        state.jamendo.isPlaying = false;
        state.jamendo.currentTime = 0;
        state.jamendo.duration = 0;
        state.jamendo.currentTrack = null;
        stopProgressUpdate();
        if (state.jamendo.audioElement) {
            state.jamendo.audioElement = null;
        }
        renderJamendoTracks();
        showToast('播放失败，请重试', 'error');
    };
    
    state.jamendo.audioElement.play().then(() => {
        state.jamendo.isPlaying = true;
        renderJamendoTracks();
        showAudioPlayer(track);
    }).catch(err => {
        console.error('播放失败:', err);
        state.jamendo.isPlaying = false;
        state.jamendo.currentTrack = null;
        state.jamendo.audioElement = null;
        renderJamendoTracks();
        showToast('播放失败，请重试', 'error');
    });
}

function previewJamendoTrack(trackId) {
    playJamendoTrack(trackId);
}

function showAudioPlayer(track) {
    // 简单的播放提示
    const trackName = track.name || '未知歌曲';
    const artistName = track.artistName || '未知艺术家';
    showToast(`正在播放: ${trackName} - ${artistName}`, 'success');
}

function hideAudioPlayer() {
    // 隐藏播放器
}

function stopJamendoPlayback() {
    if (state.jamendo.audioElement) {
        state.jamendo.audioElement.pause();
        state.jamendo.audioElement = null;
    }
    state.jamendo.isPlaying = false;
    state.jamendo.currentTime = 0;
    state.jamendo.duration = 0;
    state.jamendo.currentTrack = null;
    stopProgressUpdate();
    renderJamendoTracks();
}

// 导入Jamendo歌曲
async function importJamendoTrack(trackId) {
    const track = state.jamendo.tracks.find(t => t.id === trackId);
    if (!track) return;
    
    showLoading(true, '正在导入歌曲...');
    
    try {
        // 创建本地歌曲记录
        const songData = {
            title: track.name,
            lyrics: track.lyrics || `(Jamendo导入 - ${track.artistName})`,
            musicStyle: track.musicinfo?.tags?.genres?.[0] || '流行',
            bpm: 120, // Jamendo API不提供BPM，使用默认值
            isGenerated: false,
            externalSource: 'jamendo',
            externalId: track.id,
            externalUrl: track.shareurl,
            audioUrl: track.audio,
            coverUrl: track.image,
            artist: track.artistName,
            album: track.albumName,
            duration: track.duration,
            license: track.licenseCcurl
        };
        
        const response = await fetch(`${API_BASE}/songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(songData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            state.songs.push(data.data);
            showToast(`歌曲 "${track.name}" 已导入！`, 'success');
        } else {
            showToast(data.message || '导入失败', 'error');
        }
    } catch (error) {
        console.error('导入失败:', error);
        showToast('导入失败，请重试', 'error');
    } finally {
        showLoading(false);
    }
}

// ========================================
// 配置管理：通用模态框功能
// ========================================

function openModal(modalId) {
    document.getElementById(modalId).classList.remove('hidden');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}

function switchFormTab(formType, tabName) {
    // 切换标签按钮状态
    document.querySelectorAll(`#modal-${formType} .form-tab`).forEach(tab => {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // 切换面板显示
    document.querySelectorAll(`#modal-${formType} .form-tab-panel`).forEach(panel => {
        panel.classList.remove('active');
    });
    document.getElementById(`${formType}-tab-${tabName}`).classList.add('active');
}

function updateParamValue(paramId, value) {
    const valueEl = document.getElementById(`${paramId}-value`);
    if (valueEl) {
        valueEl.textContent = value;
    }
}

// 待删除项
let pendingDeleteType = null;
let pendingDeleteId = null;

function confirmDelete() {
    if (pendingDeleteType === 'singer') {
        deleteSinger(pendingDeleteId);
    } else if (pendingDeleteType === 'technique') {
        deleteTechnique(pendingDeleteId);
    } else if (pendingDeleteType === 'emotion') {
        deleteEmotion(pendingDeleteId);
    }
    closeModal('modal-confirm');
}

// ========================================
// AI一键创建歌手
// ========================================

let previewedSingerData = null; // 存储预览的歌手数据

function showAiGenerateSingerModal() {
    document.getElementById('ai-singer-prompt').value = '';
    document.getElementById('ai-singer-reference').value = '';
    document.getElementById('ai-singer-language').value = '中文';
    hideAiPreview();
    // 确保LLM下拉框是最新的
    updateAllLlmDropdowns();
    openModal('modal-ai-singer');
}

function fillAiPrompt(text) {
    document.getElementById('ai-singer-prompt').value = text;
}

async function generateAiSinger(event) {
    event.preventDefault();
    
    const prompt = document.getElementById('ai-singer-prompt').value.trim();
    if (!prompt) {
        showToast('请输入歌手描述', 'warning');
        return;
    }
    
    const reference = document.getElementById('ai-singer-reference').value.trim();
    const language = document.getElementById('ai-singer-language').value;
    
    // 显示加载状态
    showAiLoading();
    
    try {
        const response = await fetch(`${API_BASE}/singers/generate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                prompt: prompt,
                referenceArtist: reference || null,
                language: language
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            closeModal('modal-ai-singer');
            await loadSingers();
            renderSingersTable();
            showToast(`🎉 AI歌手 "${data.data.singer.name}" 创建成功！`, 'success');
        } else {
            hideAiLoading();
            showToast(data.message || 'AI生成失败', 'error');
        }
    } catch (error) {
        console.error('AI生成歌手失败:', error);
        hideAiLoading();
        showToast('网络错误，请重试', 'error');
    }
}

async function previewAiSinger() {
    const prompt = document.getElementById('ai-singer-prompt').value.trim();
    if (!prompt) {
        showToast('请输入歌手描述', 'warning');
        return;
    }
    
    const reference = document.getElementById('ai-singer-reference').value.trim();
    const language = document.getElementById('ai-singer-language').value;
    
    // 显示加载状态
    showAiLoading();
    
    try {
        const response = await fetch(`${API_BASE}/singers/generate/preview`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                prompt: prompt,
                referenceArtist: reference || null,
                language: language
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            hideAiLoading();
            previewedSingerData = data.data;
            showAiPreview(data.data);
        } else {
            hideAiLoading();
            showToast(data.message || '预览生成失败', 'error');
        }
    } catch (error) {
        console.error('预览生成失败:', error);
        hideAiLoading();
        showToast('网络错误，请重试', 'error');
    }
}

function showAiLoading() {
    const section = document.querySelector('.ai-generate-section');
    const form = document.getElementById('form-ai-singer');
    const preview = document.getElementById('ai-singer-preview');
    
    form.style.display = 'none';
    preview.classList.add('hidden');
    
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'ai-loading-indicator';
    loadingDiv.className = 'ai-loading';
    loadingDiv.innerHTML = `
        <div class="ai-loading-spinner"></div>
        <div class="ai-loading-text">AI正在为您创建专属歌手，请稍候...</div>
    `;
    section.appendChild(loadingDiv);
}

function hideAiLoading() {
    const form = document.getElementById('form-ai-singer');
    const loadingDiv = document.getElementById('ai-loading-indicator');
    
    form.style.display = 'block';
    if (loadingDiv) {
        loadingDiv.remove();
    }
}

function showAiPreview(data) {
    const singer = data.singer;
    const preview = document.getElementById('ai-singer-preview');
    
    document.getElementById('preview-singer-name').textContent = singer.name || '-';
    document.getElementById('preview-singer-voice-type').textContent = singer.voiceType || '-';
    document.getElementById('preview-singer-style').textContent = singer.voiceStyle || '-';
    document.getElementById('preview-singer-character').textContent = singer.voiceCharacter || '-';
    document.getElementById('preview-singer-description').textContent = singer.description || '-';
    document.getElementById('preview-singer-notes').textContent = data.designNotes || '-';
    
    preview.classList.remove('hidden');
}

function hideAiPreview() {
    previewedSingerData = null;
    document.getElementById('ai-singer-preview').classList.add('hidden');
}

async function savePreviewedSinger() {
    if (!previewedSingerData) {
        showToast('没有可保存的数据', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/singers`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(previewedSingerData.singer)
        });
        
        const data = await response.json();
        
        if (data.success) {
            closeModal('modal-ai-singer');
            await loadSingers();
            renderSingersTable();
            showToast(`🎉 AI歌手 "${previewedSingerData.singer.name}" 创建成功！`, 'success');
            previewedSingerData = null;
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存歌手失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

// ========================================
// 配置管理：歌手管理
// ========================================

function showAddSingerModal() {
    document.getElementById('modal-singer-title').textContent = '添加歌手';
    document.getElementById('singer-id').value = '';
    resetSingerForm();
    openModal('modal-singer');
}

function resetSingerForm() {
    document.getElementById('form-singer').reset();
    // 重置滑块显示值
    updateParamValue('singer-vibrato-depth', 50);
    updateParamValue('singer-vibrato-rate', 50);
    updateParamValue('singer-breathiness', 30);
    updateParamValue('singer-tension', 50);
    updateParamValue('singer-brightness', 50);
    updateParamValue('singer-gender-factor', 50);
    // 显示第一个标签
    document.querySelectorAll('#modal-singer .form-tab').forEach((tab, i) => {
        tab.classList.toggle('active', i === 0);
    });
    document.querySelectorAll('#modal-singer .form-tab-panel').forEach((panel, i) => {
        panel.classList.toggle('active', i === 0);
    });
}

async function editSinger(id) {
    const singer = state.singers.find(s => s.id === id);
    if (!singer) return;
    
    document.getElementById('modal-singer-title').textContent = '编辑歌手';
    document.getElementById('singer-id').value = singer.id;
    
    // 基本信息
    document.getElementById('singer-name').value = singer.name || '';
    document.getElementById('singer-name-en').value = singer.nameEn || '';
    document.getElementById('singer-avatar-url').value = singer.avatarUrl || '';
    document.getElementById('singer-description').value = singer.description || '';
    document.getElementById('singer-voice-type').value = singer.voiceType || '女声';
    document.getElementById('singer-voice-style').value = singer.voiceStyle || '流行';
    document.getElementById('singer-category').value = singer.category || '';
    document.getElementById('singer-voice-character').value = singer.voiceCharacter || '';
    document.getElementById('singer-tags').value = singer.tags || '';
    
    // 声音配置
    document.getElementById('singer-vocal-range-low').value = singer.vocalRangeLow || '';
    document.getElementById('singer-vocal-range-high').value = singer.vocalRangeHigh || '';
    document.getElementById('singer-tessitura-low').value = singer.tessituraLow || '';
    document.getElementById('singer-tessitura-high').value = singer.tessituraHigh || '';
    document.getElementById('singer-primary-language').value = singer.primaryLanguage || '';
    document.getElementById('singer-supported-languages').value = singer.supportedLanguages || '';
    document.getElementById('singer-dialect-support').value = singer.dialectSupport || '';
    document.getElementById('singer-technique-strength').value = singer.techniqueStrength || '';
    document.getElementById('singer-emotion-strength').value = singer.emotionStrength || '';
    document.getElementById('singer-suitable-genres').value = singer.suitableGenres || '';
    
    // 合成参数
    const vibratoDepth = singer.defaultVibratoDepth || 50;
    const vibratoRate = singer.defaultVibratoRate || 50;
    const breathiness = singer.defaultBreathiness || 30;
    const tension = singer.defaultTension || 50;
    const brightness = singer.defaultBrightness || 50;
    const genderFactor = singer.defaultGenderFactor || 50;
    
    document.getElementById('singer-default-vibrato-depth').value = vibratoDepth;
    document.getElementById('singer-default-vibrato-rate').value = vibratoRate;
    document.getElementById('singer-default-breathiness').value = breathiness;
    document.getElementById('singer-default-tension').value = tension;
    document.getElementById('singer-default-brightness').value = brightness;
    document.getElementById('singer-default-gender-factor').value = genderFactor;
    
    updateParamValue('singer-vibrato-depth', vibratoDepth);
    updateParamValue('singer-vibrato-rate', vibratoRate);
    updateParamValue('singer-breathiness', breathiness);
    updateParamValue('singer-tension', tension);
    updateParamValue('singer-brightness', brightness);
    updateParamValue('singer-gender-factor', genderFactor);
    
    // 引擎配置
    document.getElementById('singer-voice-engine').value = singer.voiceEngine || '';
    document.getElementById('singer-voice-model-version').value = singer.voiceModelVersion || '';
    document.getElementById('singer-voice-model-path').value = singer.voiceModelPath || '';
    document.getElementById('singer-model-config-json').value = singer.modelConfigJson || '';
    document.getElementById('singer-license-type').value = singer.licenseType || '免费';
    document.getElementById('singer-creator').value = singer.creator || '';
    document.getElementById('singer-original-artist').value = singer.originalArtist || '';
    document.getElementById('singer-sort-order').value = singer.sortOrder || 0;
    document.getElementById('singer-enabled').checked = singer.enabled !== false;
    
    // 显示第一个标签
    document.querySelectorAll('#modal-singer .form-tab').forEach((tab, i) => {
        tab.classList.toggle('active', i === 0);
    });
    document.querySelectorAll('#modal-singer .form-tab-panel').forEach((panel, i) => {
        panel.classList.toggle('active', i === 0);
    });
    
    openModal('modal-singer');
}

async function saveSinger() {
    const id = document.getElementById('singer-id').value;
    
    const singerData = {
        name: document.getElementById('singer-name').value,
        nameEn: document.getElementById('singer-name-en').value,
        avatarUrl: document.getElementById('singer-avatar-url').value,
        description: document.getElementById('singer-description').value,
        voiceType: document.getElementById('singer-voice-type').value,
        voiceStyle: document.getElementById('singer-voice-style').value,
        category: document.getElementById('singer-category').value,
        voiceCharacter: document.getElementById('singer-voice-character').value,
        tags: document.getElementById('singer-tags').value,
        vocalRangeLow: document.getElementById('singer-vocal-range-low').value,
        vocalRangeHigh: document.getElementById('singer-vocal-range-high').value,
        tessituraLow: document.getElementById('singer-tessitura-low').value,
        tessituraHigh: document.getElementById('singer-tessitura-high').value,
        primaryLanguage: document.getElementById('singer-primary-language').value,
        supportedLanguages: document.getElementById('singer-supported-languages').value,
        dialectSupport: document.getElementById('singer-dialect-support').value,
        techniqueStrength: document.getElementById('singer-technique-strength').value,
        emotionStrength: document.getElementById('singer-emotion-strength').value,
        suitableGenres: document.getElementById('singer-suitable-genres').value,
        defaultVibratoDepth: parseInt(document.getElementById('singer-default-vibrato-depth').value),
        defaultVibratoRate: parseInt(document.getElementById('singer-default-vibrato-rate').value),
        defaultBreathiness: parseInt(document.getElementById('singer-default-breathiness').value),
        defaultTension: parseInt(document.getElementById('singer-default-tension').value),
        defaultBrightness: parseInt(document.getElementById('singer-default-brightness').value),
        defaultGenderFactor: parseInt(document.getElementById('singer-default-gender-factor').value),
        voiceEngine: document.getElementById('singer-voice-engine').value,
        voiceModelVersion: document.getElementById('singer-voice-model-version').value,
        voiceModelPath: document.getElementById('singer-voice-model-path').value,
        modelConfigJson: document.getElementById('singer-model-config-json').value,
        licenseType: document.getElementById('singer-license-type').value,
        creator: document.getElementById('singer-creator').value,
        originalArtist: document.getElementById('singer-original-artist').value,
        sortOrder: parseInt(document.getElementById('singer-sort-order').value) || 0,
        enabled: document.getElementById('singer-enabled').checked
    };
    
    try {
        const url = id ? `${API_BASE}/singers/${id}` : `${API_BASE}/singers`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(singerData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            closeModal('modal-singer');
            await loadSingers();
            renderSingersTable();
            showToast(id ? '歌手已更新' : '歌手已添加', 'success');
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存歌手失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

function confirmDeleteSinger(id) {
    pendingDeleteType = 'singer';
    pendingDeleteId = id;
    document.getElementById('confirm-message').textContent = '确定要删除这个歌手吗？此操作不可撤销。';
    openModal('modal-confirm');
}

async function deleteSinger(id) {
    try {
        const response = await fetch(`${API_BASE}/singers/${id}`, { method: 'DELETE' });
        const data = await response.json();
        
        if (data.success) {
            state.singers = state.singers.filter(s => s.id !== id);
            renderSingersTable();
            showToast('歌手已删除', 'success');
        }
    } catch (error) {
        showToast('删除失败', 'error');
    }
}

// ========================================
// 配置管理：技巧管理
// ========================================

function showAddTechniqueModal() {
    document.getElementById('modal-technique-title').textContent = '添加技巧';
    document.getElementById('technique-id').value = '';
    resetTechniqueForm();
    openModal('modal-technique');
}

function resetTechniqueForm() {
    document.getElementById('form-technique').reset();
    updateParamValue('technique-vibrato-depth', 50);
    updateParamValue('technique-vibrato-rate', 50);
    updateParamValue('technique-breathiness', 30);
    updateParamValue('technique-tension', 50);
    updateParamValue('technique-brightness', 50);
    // 显示第一个标签
    document.querySelectorAll('#modal-technique .form-tab').forEach((tab, i) => {
        tab.classList.toggle('active', i === 0);
    });
    document.querySelectorAll('#modal-technique .form-tab-panel').forEach((panel, i) => {
        panel.classList.toggle('active', i === 0);
    });
}

async function editTechnique(id) {
    const technique = state.techniques.find(t => t.id === id);
    if (!technique) return;
    
    document.getElementById('modal-technique-title').textContent = '编辑技巧';
    document.getElementById('technique-id').value = technique.id;
    
    // 基本信息
    document.getElementById('technique-technique-id').value = technique.techniqueId || '';
    document.getElementById('technique-name').value = technique.name || '';
    document.getElementById('technique-name-en').value = technique.nameEn || '';
    document.getElementById('technique-category').value = technique.category || '基础';
    document.getElementById('technique-description').value = technique.description || '';
    document.getElementById('technique-difficulty-level').value = technique.difficultyLevel || 1;
    document.getElementById('technique-sort-order').value = technique.sortOrder || 0;
    document.getElementById('technique-enabled').checked = technique.enabled !== false;
    
    // 合成参数
    const vibratoDepth = technique.vibratoDepth || 50;
    const vibratoRate = technique.vibratoRate || 50;
    const breathiness = technique.breathiness || 30;
    const tension = technique.tension || 50;
    const brightness = technique.brightness || 50;
    
    document.getElementById('technique-vibrato-depth').value = vibratoDepth;
    document.getElementById('technique-vibrato-rate').value = vibratoRate;
    document.getElementById('technique-breathiness').value = breathiness;
    document.getElementById('technique-tension').value = tension;
    document.getElementById('technique-brightness').value = brightness;
    document.getElementById('technique-phonation-type').value = technique.phonationType || 'normal';
    document.getElementById('technique-pitch-bend-range').value = technique.pitchBendRange || 100;
    
    updateParamValue('technique-vibrato-depth', vibratoDepth);
    updateParamValue('technique-vibrato-rate', vibratoRate);
    updateParamValue('technique-breathiness', breathiness);
    updateParamValue('technique-tension', tension);
    updateParamValue('technique-brightness', brightness);
    
    // LLM Prompt
    document.getElementById('technique-prompt-description').value = technique.promptDescription || '';
    
    // 显示第一个标签
    document.querySelectorAll('#modal-technique .form-tab').forEach((tab, i) => {
        tab.classList.toggle('active', i === 0);
    });
    document.querySelectorAll('#modal-technique .form-tab-panel').forEach((panel, i) => {
        panel.classList.toggle('active', i === 0);
    });
    
    openModal('modal-technique');
}

async function saveTechnique() {
    const id = document.getElementById('technique-id').value;
    
    const techniqueData = {
        techniqueId: document.getElementById('technique-technique-id').value,
        name: document.getElementById('technique-name').value,
        nameEn: document.getElementById('technique-name-en').value,
        category: document.getElementById('technique-category').value,
        description: document.getElementById('technique-description').value,
        difficultyLevel: parseInt(document.getElementById('technique-difficulty-level').value),
        vibratoDepth: parseInt(document.getElementById('technique-vibrato-depth').value),
        vibratoRate: parseInt(document.getElementById('technique-vibrato-rate').value),
        breathiness: parseInt(document.getElementById('technique-breathiness').value),
        tension: parseInt(document.getElementById('technique-tension').value),
        brightness: parseInt(document.getElementById('technique-brightness').value),
        phonationType: document.getElementById('technique-phonation-type').value,
        pitchBendRange: parseInt(document.getElementById('technique-pitch-bend-range').value),
        promptDescription: document.getElementById('technique-prompt-description').value,
        sortOrder: parseInt(document.getElementById('technique-sort-order').value) || 0,
        enabled: document.getElementById('technique-enabled').checked
    };
    
    try {
        const url = id ? `${API_BASE}/techniques/${id}` : `${API_BASE}/techniques`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(techniqueData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            closeModal('modal-technique');
            await loadTechniques();
            renderTechniquesCards();
            showToast(id ? '技巧已更新' : '技巧已添加', 'success');
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存技巧失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

function confirmDeleteTechnique(id) {
    pendingDeleteType = 'technique';
    pendingDeleteId = id;
    document.getElementById('confirm-message').textContent = '确定要删除这个技巧吗？此操作不可撤销。';
    openModal('modal-confirm');
}

async function deleteTechnique(id) {
    try {
        const response = await fetch(`${API_BASE}/techniques/${id}`, { method: 'DELETE' });
        const data = await response.json();
        
        if (data.success) {
            state.techniques = state.techniques.filter(t => t.id !== id);
            renderTechniquesCards();
            showToast('技巧已删除', 'success');
        }
    } catch (error) {
        showToast('删除失败', 'error');
    }
}

// ========================================
// 配置管理：情绪管理
// ========================================

function showAddEmotionModal() {
    document.getElementById('modal-emotion-title').textContent = '添加情绪';
    document.getElementById('emotion-id').value = '';
    resetEmotionForm();
    openModal('modal-emotion');
}

function resetEmotionForm() {
    document.getElementById('form-emotion').reset();
    updateParamValue('emotion-intensity', 50);
    // 显示第一个标签
    document.querySelectorAll('#modal-emotion .form-tab').forEach((tab, i) => {
        tab.classList.toggle('active', i === 0);
    });
    document.querySelectorAll('#modal-emotion .form-tab-panel').forEach((panel, i) => {
        panel.classList.toggle('active', i === 0);
    });
}

async function editEmotion(id) {
    const emotion = state.emotions.find(e => e.id === id);
    if (!emotion) return;
    
    document.getElementById('modal-emotion-title').textContent = '编辑情绪';
    document.getElementById('emotion-id').value = emotion.id;
    
    // 基本信息
    document.getElementById('emotion-emotion-id').value = emotion.emotionId || '';
    document.getElementById('emotion-name').value = emotion.name || '';
    document.getElementById('emotion-name-en').value = emotion.nameEn || '';
    document.getElementById('emotion-category').value = emotion.category || '中性';
    document.getElementById('emotion-description').value = emotion.description || '';
    document.getElementById('emotion-color-code').value = emotion.colorCode || '#FFD700';
    document.getElementById('emotion-icon-name').value = emotion.iconName || '';
    document.getElementById('emotion-sort-order').value = emotion.sortOrder || 0;
    document.getElementById('emotion-enabled').checked = emotion.enabled !== false;
    
    // 合成参数
    const intensity = emotion.intensity || 50;
    document.getElementById('emotion-intensity').value = intensity;
    updateParamValue('emotion-intensity', intensity);
    
    document.getElementById('emotion-pitch-variance').value = emotion.pitchVariance || 1.0;
    document.getElementById('emotion-energy-multiplier').value = emotion.energyMultiplier || 1.0;
    document.getElementById('emotion-tempo-factor').value = emotion.tempoFactor || 1.0;
    document.getElementById('emotion-vibrato-depth-modifier').value = emotion.vibratoDepthModifier || 1.0;
    document.getElementById('emotion-tension-modifier').value = emotion.tensionModifier || 1.0;
    
    // LLM Prompt
    document.getElementById('emotion-prompt-description').value = emotion.promptDescription || '';
    document.getElementById('emotion-prompt-keywords').value = emotion.promptKeywords || '';
    
    // 显示第一个标签
    document.querySelectorAll('#modal-emotion .form-tab').forEach((tab, i) => {
        tab.classList.toggle('active', i === 0);
    });
    document.querySelectorAll('#modal-emotion .form-tab-panel').forEach((panel, i) => {
        panel.classList.toggle('active', i === 0);
    });
    
    openModal('modal-emotion');
}

async function saveEmotion() {
    const id = document.getElementById('emotion-id').value;
    
    const emotionData = {
        emotionId: document.getElementById('emotion-emotion-id').value,
        name: document.getElementById('emotion-name').value,
        nameEn: document.getElementById('emotion-name-en').value,
        category: document.getElementById('emotion-category').value,
        description: document.getElementById('emotion-description').value,
        colorCode: document.getElementById('emotion-color-code').value,
        iconName: document.getElementById('emotion-icon-name').value,
        intensity: parseInt(document.getElementById('emotion-intensity').value),
        pitchVariance: parseFloat(document.getElementById('emotion-pitch-variance').value),
        energyMultiplier: parseFloat(document.getElementById('emotion-energy-multiplier').value),
        tempoFactor: parseFloat(document.getElementById('emotion-tempo-factor').value),
        vibratoDepthModifier: parseFloat(document.getElementById('emotion-vibrato-depth-modifier').value),
        tensionModifier: parseFloat(document.getElementById('emotion-tension-modifier').value),
        promptDescription: document.getElementById('emotion-prompt-description').value,
        promptKeywords: document.getElementById('emotion-prompt-keywords').value,
        sortOrder: parseInt(document.getElementById('emotion-sort-order').value) || 0,
        enabled: document.getElementById('emotion-enabled').checked
    };
    
    try {
        const url = id ? `${API_BASE}/emotions/${id}` : `${API_BASE}/emotions`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(emotionData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            closeModal('modal-emotion');
            await loadEmotions();
            renderEmotionsCards();
            showToast(id ? '情绪已更新' : '情绪已添加', 'success');
        } else {
            showToast(data.message || '保存失败', 'error');
        }
    } catch (error) {
        console.error('保存情绪失败:', error);
        showToast('保存失败，请重试', 'error');
    }
}

function confirmDeleteEmotion(id) {
    pendingDeleteType = 'emotion';
    pendingDeleteId = id;
    document.getElementById('confirm-message').textContent = '确定要删除这个情绪吗？此操作不可撤销。';
    openModal('modal-confirm');
}

async function deleteEmotion(id) {
    try {
        const response = await fetch(`${API_BASE}/emotions/${id}`, { method: 'DELETE' });
        const data = await response.json();
        
        if (data.success) {
            state.emotions = state.emotions.filter(e => e.id !== id);
            renderEmotionsCards();
            showToast('情绪已删除', 'success');
        }
    } catch (error) {
        showToast('删除失败', 'error');
    }
}
