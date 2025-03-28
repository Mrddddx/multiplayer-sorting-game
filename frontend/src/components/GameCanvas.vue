<template>
    <div class="game-container">
    <!-- 加入阶段 -->
    <div v-if="!joined" class="join-screen">
        <h2>加入排序游戏</h2>
        <input v-model="username" placeholder="请输入昵称" />
        <button @click="joinGame" :disabled="!username">加入</button>
    </div>

    <!-- 准备阶段 -->
    <div v-else-if="!started">
        <h2>等待开始...</h2>
        <p>你好，<strong>{{ username }}</strong></p>
        <ul>
        <li v-for="p in players" :key="p.id">
            {{ p.name }} - {{ p.ready ? "✅ 已准备" : "⏳ 未准备" }}
        </li>
        </ul>
        <button @click="sendReady" :disabled="ready">准备</button>
    </div>

    <!-- 游戏阶段 -->
    <div v-else class="play-area">
        <h2>用 ← 和 → 交换位置进行排序！</h2>
        <div class="queue">
        <div
            v-for="p in players"
            :key="p.id"
            class="player"
            :class="{ me: p.id === myId }"
            :style="{
            width: p.height * 0.6 + 'px',
            height: (p.height - 150) * 6.2 + 'px'
            }"
        >
            <div>{{ p.height }}cm</div>
            <small>{{ p.id === myId ? '你 (' + p.name + ')' : p.name }}</small>
        </div>
        </div>

        <div v-if="gameOver" class="overlay">
        🎉 排序完成！
        </div>
    </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const username = ref('')
const joined = ref(false)
const ready = ref(false)
const started = ref(false)
const gameOver = ref(false)

const myId = ref('')
const players = ref([])
let socket

function joinGame() {
    socket = new WebSocket('ws://192.168.3.127:8080/ws')
    joined.value = true

    socket.onopen = () => {
    socket.send(JSON.stringify({ type: 'join', name: username.value }))
    }

    socket.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.type === 'update') {
        players.value = data.queue
        started.value = data.started
        myId.value = data.yourId

        if (players.value.every(p => !p.ready)) {
        started.value = false
        ready.value = false
        }
    } else if (data.type === 'start') {
        started.value = true
    } else if (data.type === 'gameOver') {
        gameOver.value = true
        setTimeout(() => {
        gameOver.value = false
        }, 2000)
    }
    }
}

function sendReady() {
    socket.send(JSON.stringify({ type: 'ready' }))
    ready.value = true
}

function handleKey(e) {
    if (!started.value || gameOver.value) return
    if (e.key === 'ArrowLeft') {
    socket.send(JSON.stringify({ type: 'swapLeft' }))
    } else if (e.key === 'ArrowRight') {
    socket.send(JSON.stringify({ type: 'swapRight' }))
    }
}

onMounted(() => {
    window.addEventListener('keydown', handleKey)
})

onUnmounted(() => {
    window.removeEventListener('keydown', handleKey)
    socket?.close()
})
</script>

<style scoped>
.game-container {
    padding: 20px;
    text-align: center;
    font-family: sans-serif;
    min-width: 100vh;
}

.join-screen input {
    padding: 8px;
    font-size: 16px;
    margin-right: 8px;
}

button {
    padding: 8px 16px;
    font-size: 16px;
    margin-top: 10px;
    cursor: pointer;
}

.queue {
    display: flex;
    justify-content: center;
    align-items: flex-end;
    gap: 10px;
    margin-top: 30px;
    flex-wrap: wrap;
}

.player {
    background: #e0f0ff;
    border: 2px solid #3399ff;
    color: #222;
    border-radius: 8px;
    padding: 4px;
    text-align: center;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    transition: all 0.2s ease;
}

.player.me {
    background: #ffe0e0;
    border-color: red;
    font-weight: bold;
}

.overlay {
    font-size: 24px;
    color: green;
    margin-top: 20px;
    font-weight: bold;
}
</style>
