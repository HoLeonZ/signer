# 🎤 AI Singer Studio - 智能歌手创作平台

一个配置化的AI歌手网站，支持歌手声音选择、歌曲管理、AI歌词生成以及音乐片段的精细化配置。

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![License](https://img.shields.io/badge/License-MIT-blue)

## ✨ 功能特性

### 🚀 快速开始向导
- **4步向导式创作流程**：选择歌手 → 创建歌曲 → 配置演唱 → 预览生成
- **降低使用门槛**：适合新手用户快速上手

### 📋 歌曲模板库
- **8种预设风格模板**：甜蜜情歌、励志摇滚、古风仙侠、清新民谣等
- **一键加载模板**：自动预填充创作参数
- **模板使用统计**：查看热门模板

### 📁 项目管理
- **保存/加载项目**：随时保存创作进度
- **项目导出**：导出为JSON配置文件
- **草稿/完成状态**：管理项目状态

### 🎙️ 多样化AI歌手
- 支持多种声音类型（女声、男声、中性）
- 不同演唱风格（流行、摇滚、古风、民谣等）
- 预置6位AI歌手，可自由扩展

### 🎵 歌曲管理
- 创建和管理歌曲库
- 支持按歌手、风格筛选
- 歌曲搜索功能

### ✨ AI歌词创作（多LLM支持）
- **三种LLM引擎**：通义千问、OpenAI、Google Gemini
- 运行时动态切换AI引擎
- 支持主题、情绪、风格等多维度配置
- 自动分段并提供演唱建议

### 🎚️ 片段精细化编辑
- 为每个音乐片段独立配置演唱技巧
- 10种专业演唱技巧（颤音、假声、混声等）
- 10种演唱情绪（快乐、忧伤、深情等）
- 音量和音高调节

### ⚙️ 后台管理
- 歌手/歌曲/技巧/情绪的CRUD管理
- LLM引擎配置与切换
- 系统设置面板

## 🛠️ 技术栈

### 后端
- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (开发环境，可替换为MySQL/PostgreSQL)
- **WebFlux** (用于调用LLM API)
- **Lombok**

### 前端
- **原生 HTML5 / CSS3 / JavaScript**
- **现代星空主题UI设计**
- **响应式布局**
- **CSS动画效果**

## 📁 项目结构

```
AISinger/
├── pom.xml                          # Maven配置
├── README.md                        # 项目说明
├── src/main/java/com/aisinger/
│   ├── AiSingerApplication.java     # 应用入口
│   ├── config/
│   │   └── DataInitializer.java     # 数据初始化
│   ├── controller/                  # REST控制器
│   │   ├── SingerController.java
│   │   ├── SongController.java
│   │   ├── SegmentController.java
│   │   ├── TechniqueController.java
│   │   └── EmotionController.java
│   ├── dto/                         # 数据传输对象
│   │   ├── ApiResponse.java
│   │   ├── SongCreateRequest.java
│   │   ├── LyricsGenerateRequest.java
│   │   ├── LyricsGenerateResponse.java
│   │   └── SegmentUpdateRequest.java
│   ├── entity/                      # 实体类
│   │   ├── Singer.java
│   │   ├── Song.java
│   │   ├── MusicSegment.java
│   │   ├── SingingTechnique.java
│   │   └── Emotion.java
│   ├── repository/                  # 数据仓库
│   └── service/                     # 业务服务
│       ├── SingerService.java
│       ├── SongService.java
│       ├── MusicSegmentService.java
│       ├── TechniqueService.java
│       ├── EmotionService.java
│       └── LlmService.java          # LLM歌词生成服务
└── src/main/resources/
    ├── application.yml              # 应用配置
    └── static/                      # 前端静态资源
        ├── index.html
        ├── css/style.css
        └── js/app.js
```

## 🚀 快速开始

### 环境要求
- JDK 17 或更高版本
- Maven 3.6+

### 运行步骤

1. **克隆项目**
```bash
cd /path/to/AISinger
```

2. **配置LLM API（可选）**

编辑 `src/main/resources/application.yml`：
```yaml
llm:
  qwen:
    api-key: your-dashscope-api-key
```

或设置环境变量：
```bash
export OPENAI_API_KEY=your-api-key
```

3. **运行应用**
```bash
./mvnw spring-boot:run
```

或使用Maven：
```bash
mvn spring-boot:run
```

4. **访问应用**

打开浏览器访问：http://localhost:8080

## 📚 API接口

### 歌手管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/singers | 获取所有歌手 |
| GET | /api/singers/{id} | 获取单个歌手 |
| POST | /api/singers | 创建歌手 |
| PUT | /api/singers/{id} | 更新歌手 |
| DELETE | /api/singers/{id} | 删除歌手 |

### 歌曲管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/songs | 获取所有歌曲 |
| GET | /api/songs/{id} | 获取单个歌曲（含片段） |
| POST | /api/songs | 创建歌曲 |
| POST | /api/songs/generate-lyrics | AI生成歌词 |
| DELETE | /api/songs/{id} | 删除歌曲 |

### 片段编辑
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/segments/song/{songId} | 获取歌曲的所有片段 |
| PUT | /api/segments/{id} | 更新片段配置 |

### 技巧与情绪
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/techniques | 获取所有演唱技巧 |
| GET | /api/emotions | 获取所有演唱情绪 |

## 🎨 预置数据

### 演唱技巧
1. 自然音 - 基础演唱方式
2. 气声 - 带有气息感的演唱
3. 颤音 - 音高快速轻微波动
4. 假声 - 高音区轻柔演唱
5. 混声 - 真假声混合
6. 转音 - 快速音高变化
7. 怒音 - 嘶吼感演唱
8. 哭腔 - 带哭泣感
9. 咽音 - 咽部共鸣
10. 海豚音 - 极高音哨音

### 演唱情绪
1. 平静 ☮️ - 平和安宁
2. 快乐 😊 - 欢快愉悦
3. 兴奋 🔥 - 激动亢奋
4. 深情 💕 - 温柔深情
5. 忧伤 😢 - 悲伤忧郁
6. 愤怒 😠 - 激烈愤怒
7. 思念 🌙 - 思念渴望
8. 希望 🌈 - 充满期待
9. 释然 🕊️ - 释怀解脱
10. 神秘 🌌 - 神秘深邃

## 🔧 配置说明

### 数据库配置
默认使用H2内存数据库。如需持久化，可修改配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aisinger
    username: root
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### LLM服务配置（多提供商支持）

本项目支持三种LLM提供商，可通过配置切换：

```yaml
llm:
  # 当前启用的提供商: qwen, openai, gemini
  provider: qwen
  
  # 阿里通义千问
  qwen:
    api-key: ${QWEN_API_KEY}
    api-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
    model: qwen-turbo
  
  # OpenAI
  openai:
    api-key: ${OPENAI_API_KEY}
    api-url: https://api.openai.com/v1/chat/completions
    model: gpt-3.5-turbo
  
  # Google Gemini
  gemini:
    api-key: ${GEMINI_API_KEY}
    api-url: https://generativelanguage.googleapis.com/v1beta/models
    model: gemini-pro
```

设置环境变量（根据需要设置）：
```bash
export QWEN_API_KEY=your-dashscope-api-key
export OPENAI_API_KEY=your-openai-api-key
export GEMINI_API_KEY=your-gemini-api-key
```

获取API Key：
- **通义千问**: [阿里云灵积平台](https://dashscope.console.aliyun.com/)
- **OpenAI**: [OpenAI Platform](https://platform.openai.com/)
- **Gemini**: [Google AI Studio](https://makersuite.google.com/)

## 📝 开发计划

- [ ] 添加音频文件上传和预览
- [ ] 集成真实的AI歌声合成引擎（如So-VITS-SVC）
- [ ] 添加用户认证和权限管理
- [ ] 支持歌曲导出和分享
- [ ] 添加更多音乐风格模板

## 📄 许可证

MIT License

## 🙏 致谢

- Spring Boot 团队
- 阿里云通义千问 / 灵积平台
- Maven阿里云镜像服务
# signer
