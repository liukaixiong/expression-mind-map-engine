/**
 * 五彩气泡碰撞动画
 * 使用方法：在页面中引入此JS文件，并确保有 id="bubbleContainer" 的容器
 *
 * 开关控制API：
 * - window.bubbleAnimation.toggle() - 切换气泡显示/隐藏
 * - window.bubbleAnimation.show() - 显示气泡
 * - window.bubbleAnimation.hide() - 隐藏气泡
 * - window.bubbleAnimation.isVisible - 查看当前显示状态
 */
(function() {
    // 等待DOM加载完成
    function initBubbleAnimation() {
        const container = document.getElementById('bubbleContainer');
        if (!container) {
            console.warn('未找到 #bubbleContainer 容器，气泡动画无法初始化');
            return;
        }

        const bubbles = [];
        const bubbleCount = 15;
        let isAnimationRunning = true; // 气泡显示状态
        let animationFrameId = null;

        // localStorage 存储键名
        const BUBBLE_VISIBLE_KEY = 'bubble_animation_visible';

        // 从 localStorage 读取初始状态（默认为 true，即显示）
        function getStoredVisibility() {
            try {
                const stored = localStorage.getItem(BUBBLE_VISIBLE_KEY);
                if (stored === null) {
                    return true; // 默认显示
                }
                return stored === 'true';
            } catch (e) {
                console.warn('无法读取 localStorage，使用默认值:', e);
                return true;
            }
        }

        // 保存可见性状态到 localStorage
        function saveVisibility(isVisible) {
            try {
                localStorage.setItem(BUBBLE_VISIBLE_KEY, String(isVisible));
            } catch (e) {
                console.warn('无法保存到 localStorage:', e);
            }
        }

        // 初始化显示状态
        isAnimationRunning = getStoredVisibility();

        // 鼠标位置
        let mouseX = -1000;
        let mouseY = -1000;
        const mouseInfluenceRadius = 250;
        const mouseAttractionForce = 0.3;

        // 淡紫色为主的配色方案
        const colors = [
            // 淡紫色系（占大多数）
            { inner: 'rgba(155, 89, 182, 1)', outer: 'rgba(142, 68, 173, 0.95)', glow: 'rgba(155, 89, 182, 0.8)' },
            { inner: 'rgba(168, 109, 195, 1)', outer: 'rgba(155, 89, 182, 0.95)', glow: 'rgba(168, 109, 195, 0.8)' },
            { inner: 'rgba(181, 126, 220, 1)', outer: 'rgba(168, 109, 195, 0.95)', glow: 'rgba(181, 126, 220, 0.8)' },
            { inner: 'rgba(139, 92, 246, 1)', outer: 'rgba(124, 58, 237, 0.95)', glow: 'rgba(139, 92, 246, 0.8)' },
            { inner: 'rgba(192, 132, 252, 1)', outer: 'rgba(168, 85, 247, 0.95)', glow: 'rgba(192, 132, 252, 0.8)' },
            { inner: 'rgba(217, 70, 239, 1)', outer: 'rgba(192, 38, 211, 0.95)', glow: 'rgba(217, 70, 239, 0.8)' },
            { inner: 'rgba(236, 72, 153, 1)', outer: 'rgba(219, 39, 119, 0.95)', glow: 'rgba(236, 72, 153, 0.8)' },
            { inner: 'rgba(129, 140, 248, 1)', outer: 'rgba(99, 102, 241, 0.95)', glow: 'rgba(129, 140, 248, 0.8)' },
            // 少量点缀色
            { inner: 'rgba(52, 152, 219, 1)', outer: 'rgba(41, 128, 185, 0.95)', glow: 'rgba(52, 152, 219, 0.8)' },
            { inner: 'rgba(85, 239, 196, 1)', outer: 'rgba(0, 206, 201, 0.95)', glow: 'rgba(85, 239, 196, 0.8)' }
        ];

        // 鼠标移动事件
        document.addEventListener('mousemove', (e) => {
            mouseX = e.clientX;
            mouseY = e.clientY;
        });

        class Bubble {
            constructor(index, total, gridPositions) {
                this.element = document.createElement('div');
                this.element.className = 'bubble';

                // 固定大小 100px
                this.size = 100;
                this.element.style.width = this.size + 'px';
                this.element.style.height = this.size + 'px';

                // 随机颜色（淡紫色系概率更高）
                const colorIndex = Math.floor(Math.random() * colors.length);
                this.color = colors[colorIndex];
                this.element.style.background = `radial-gradient(circle at 30% 30%, ${this.color.inner}, ${this.color.outer})`;
                this.element.style.boxShadow = `
                    inset 0 0 30px rgba(255, 255, 255, 0.8),
                    inset 0 0 60px rgba(255, 255, 255, 0.4),
                    inset -10px -10px 30px rgba(0, 0, 0, 0.15),
                    0 10px 30px rgba(0, 0, 0, 0.2),
                    0 20px 60px rgba(0, 0, 0, 0.1),
                    0 0 35px ${this.color.glow}
                `;

                // 使用预设的网格位置，确保分布均匀
                if (gridPositions && gridPositions[index]) {
                    this.x = gridPositions[index].x;
                    this.y = gridPositions[index].y;
                } else {
                    // 回退到随机位置
                    this.x = Math.random() * (window.innerWidth - this.size);
                    this.y = Math.random() * (window.innerHeight - this.size);
                }

                // 随机速度 (-2 到 2)
                this.vx = (Math.random() - 0.5) * 4;
                this.vy = (Math.random() - 0.5) * 4;

                // 确保速度不为0且方向多样
                if (Math.abs(this.vx) < 0.5) this.vx = (index % 2 === 0 ? 1 : -1) * (0.8 + Math.random() * 0.5);
                if (Math.abs(this.vy) < 0.5) this.vy = (index % 3 === 0 ? 1 : -1) * (0.8 + Math.random() * 0.5);

                container.appendChild(this.element);
                this.updatePosition();
            }

            updatePosition() {
                this.element.style.transform = `translate(${this.x}px, ${this.y}px)`;
            }

            move() {
                // 计算到鼠标的距离和方向
                const centerX = this.x + this.size / 2;
                const centerY = this.y + this.size / 2;
                const dx = mouseX - centerX;
                const dy = mouseY - centerY;
                const distance = Math.sqrt(dx * dx + dy * dy);

                // 如果在鼠标影响范围内，向鼠标方向加速
                if (distance < mouseInfluenceRadius && distance > 0) {
                    const force = (1 - distance / mouseInfluenceRadius) * mouseAttractionForce;
                    this.vx += (dx / distance) * force;
                    this.vy += (dy / distance) * force;
                }

                // 限制最大速度
                const maxSpeed = 4;
                const speed = Math.sqrt(this.vx * this.vx + this.vy * this.vy);
                if (speed > maxSpeed) {
                    this.vx = (this.vx / speed) * maxSpeed;
                    this.vy = (this.vy / speed) * maxSpeed;
                }

                this.x += this.vx;
                this.y += this.vy;

                // 边界碰撞检测
                if (this.x <= 0 || this.x >= window.innerWidth - this.size) {
                    this.vx *= -1;
                    this.x = Math.max(0, Math.min(this.x, window.innerWidth - this.size));
                }
                if (this.y <= 0 || this.y >= window.innerHeight - this.size) {
                    this.vy *= -1;
                    this.y = Math.max(0, Math.min(this.y, window.innerHeight - this.size));
                }

                this.updatePosition();
            }
        }

        // 检测气泡间碰撞
        function checkCollisions() {
            for (let i = 0; i < bubbles.length; i++) {
                for (let j = i + 1; j < bubbles.length; j++) {
                    const b1 = bubbles[i];
                    const b2 = bubbles[j];

                    const dx = (b1.x + b1.size / 2) - (b2.x + b2.size / 2);
                    const dy = (b1.y + b1.size / 2) - (b2.y + b2.size / 2);
                    const distance = Math.sqrt(dx * dx + dy * dy);
                    const minDistance = (b1.size + b2.size) / 2;

                    if (distance < minDistance) {
                        // 碰撞响应 - 交换速度
                        const tempVx = b1.vx;
                        const tempVy = b1.vy;
                        b1.vx = b2.vx;
                        b1.vy = b2.vy;
                        b2.vx = tempVx;
                        b2.vy = tempVy;

                        // 分离重叠的气泡
                        const overlap = minDistance - distance;
                        const nx = dx / distance;
                        const ny = dy / distance;
                        b1.x += nx * overlap / 2;
                        b1.y += ny * overlap / 2;
                        b2.x -= nx * overlap / 2;
                        b2.y -= ny * overlap / 2;
                    }
                }
            }
        }

        // 生成网格分布位置，避免气泡重叠
        function generateGridPositions(count) {
            const positions = [];
            const cols = Math.ceil(Math.sqrt(count * (window.innerWidth / window.innerHeight)));
            const rows = Math.ceil(count / cols);
            const cellWidth = window.innerWidth / cols;
            const cellHeight = window.innerHeight / rows;

            for (let i = 0; i < count; i++) {
                const col = i % cols;
                const row = Math.floor(i / cols);

                // 在单元格内随机偏移，保持一定的分散性
                const offsetX = cellWidth * 0.1 + Math.random() * cellWidth * 0.8;
                const offsetY = cellHeight * 0.1 + Math.random() * cellHeight * 0.8;

                positions.push({
                    x: col * cellWidth + offsetX - 50,
                    y: row * cellHeight + offsetY - 50
                });
            }

            return positions;
        }

        // 初始化气泡（使用网格分布）
        const gridPositions = generateGridPositions(bubbleCount);
        for (let i = 0; i < bubbleCount; i++) {
            bubbles.push(new Bubble(i, bubbleCount, gridPositions));
        }

        // 根据初始状态设置容器显示
        container.style.display = isAnimationRunning ? 'block' : 'none';

        // 动画循环
        function animate() {
            if (isAnimationRunning) {
                bubbles.forEach(bubble => bubble.move());
                checkCollisions();
            }
            animationFrameId = requestAnimationFrame(animate);
        }

        animate();

        // 自动创建并插入开关按钮（灰色气泡+X图标）
        function createToggleButton() {
            // 避免重复创建
            if (document.getElementById('bubbleToggleBtn')) {
                return;
            }

            const toggleBtn = document.createElement('div');
            toggleBtn.id = 'bubbleToggleBtn';
            toggleBtn.className = 'bubble-toggle-btn';
            toggleBtn.innerHTML = '<span class="toggle-icon">×</span>';
            // 根据存储的状态设置按钮初始状态
            const initialVisibility = isAnimationRunning;
            toggleBtn.title = initialVisibility ? '隐藏气泡' : '显示气泡';
            toggleBtn.classList.toggle('bubble-hidden', !initialVisibility);
            // 直接设置内联样式确保在右上角
            toggleBtn.style.position = 'fixed';
            toggleBtn.style.top = '5px';
            toggleBtn.style.right = '10px';
            toggleBtn.style.left = 'auto';
            toggleBtn.style.bottom = 'auto';
            document.body.appendChild(toggleBtn);

            toggleBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                window.bubbleAnimation.toggle();
                this.classList.toggle('bubble-hidden', !window.bubbleAnimation.isVisible);
                this.title = window.bubbleAnimation.isVisible ? '隐藏气泡' : '显示气泡';
            });
        }

        // 延迟创建按钮，确保body已加载
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', createToggleButton);
        } else {
            // 使用setTimeout确保在body之后插入
            setTimeout(createToggleButton, 0);
        }

        // 窗口大小改变时重新定位
        window.addEventListener('resize', () => {
            bubbles.forEach(bubble => {
                bubble.x = Math.min(bubble.x, window.innerWidth - bubble.size);
                bubble.y = Math.min(bubble.y, window.innerHeight - bubble.size);
            });
        });

        // 暴露到全局，方便外部调用
        window.bubbleAnimation = {
            bubbles,
            colors,
            isVisible: isAnimationRunning, // 从存储的状态读取初始值

            // 切换气泡显示/隐藏
            toggle: function() {
                this.isVisible = !this.isVisible;
                isAnimationRunning = this.isVisible;
                container.style.display = this.isVisible ? 'block' : 'none';
                saveVisibility(this.isVisible); // 保存状态到 localStorage
                return this.isVisible;
            },

            // 显示气泡
            show: function() {
                this.isVisible = true;
                isAnimationRunning = true;
                container.style.display = 'block';
                saveVisibility(true); // 保存状态到 localStorage
            },

            // 隐藏气泡
            hide: function() {
                this.isVisible = false;
                isAnimationRunning = false;
                container.style.display = 'none';
                saveVisibility(false); // 保存状态到 localStorage
            },

            addBubble: function() {
                bubbles.push(new Bubble());
            },
            removeBubble: function() {
                if (bubbles.length > 0) {
                    const bubble = bubbles.pop();
                    if (bubble && bubble.element) {
                        bubble.element.remove();
                    }
                }
            },
            setBubbleCount: function(count) {
                while (bubbles.length < count) {
                    bubbles.push(new Bubble());
                }
                while (bubbles.length > count) {
                    const bubble = bubbles.pop();
                    if (bubble && bubble.element) {
                        bubble.element.remove();
                    }
                }
            }
        };
    }

    // 等待DOM加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initBubbleAnimation);
    } else {
        // DOM已经加载完成
        initBubbleAnimation();
    }
})();
