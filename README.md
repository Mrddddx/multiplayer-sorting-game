# 🎮 实时多人排序游戏（局域网 WebSocket 联机）

本项目是一个基于 Spring Boot + Vue3 实现的**多人联机排序游戏**：
- 支持局域网多设备同时接入
- 玩家排队，使用键盘交换位置
- 身高不同方块大小不同，目标是排序完成！

---

## 🧩 技术栈

| 模块 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Composition API |
| 后端 | Spring Boot + WebSocket |
| 通信 | 原生 WebSocket 协议 |
| 样式 | 简单 CSS scoped 样式 |

---

## 🚀 快速启动

### 🔧 后端（Spring Boot）

```bash
cd backend
./mvnw spring-boot:run
```
默认运行在 `http://localhost:8080/ws`

确保局域网设备能访问本机（记得关闭防火墙或开启端口）

### 🌐 前端（Vue3）

```bash
cd frontend
npm install
npm run dev
```
默认运行在 `http://localhost:5173`

> 使用手机或其他电脑访问时，替换为你后端机器的局域网 IP：
>
> `ws://192.168.x.x:8080/ws`

---

## 🎮 游戏规则

1. 玩家进入后输入昵称
2. 所有人点击“准备”后，游戏开始
3. 每人使用 **← / →** 键交换与左右玩家的位置
4. 排序成功时显示“🎉 排序完成”提示（自动消失）
5. 系统自动进入下一轮准备阶段

---

## 📁 项目结构

```
project-root/
├── backend/                 # Spring Boot 后端
│   ├── config/              # WebSocket 配置类
│   └── handler/             # WebSocket 消息处理器
│   └── MultiplayerApplication.java
│   └── application.yml
│
├── frontend/                # Vue 3 前端
│   ├── index.html
│   ├── main.js
│   └── components/
│       └── GameCanvas.vue   # 主游戏组件
```

---

## 🔄 后续改进方向

- [ ] 房间机制：多个房间并行排序比赛
- [ ] 玩家计时 & 排行榜
- [ ] AI 模式 / 单人练习
- [ ] 移动端手势支持
- [ ] 更丰富的视觉样式 & 音效

---

## 📸 截图预览

> ✅ 可添加实际运行截图、多人同时联机效果

---

## 🧠 作者提示

> 本项目适合作为教学演示 / 算法可视化 / 游戏开发入门项目。
> 你可以自由魔改、扩展、优化 UI。

欢迎学习、使用并添加你自己的创意！✨

---

## 📄 License

MIT License（可自由学习、使用和修改）

