/**
 * 五彩气泡碰撞动画
 * 使用方法：在页面中引入此JS文件，并确保有 id="bubbleContainer" 的容器
 *
 * 开关控制API：
 * - window.bubbleAnimation.toggle() - 切换气泡显示/隐藏
 * - window.bubbleAnimation.show() - 显示气泡
 * - window.bubbleAnimation.hide() - 隐藏气泡（仅隐藏，不销毁）
 * - window.bubbleAnimation.destroy() - 完全销毁气泡，释放所有资源
 * - window.bubbleAnimation.reinit() - 重新初始化气泡（需先 destroy）
 * - window.bubbleAnimation.isVisible - 查看当前显示状态
 * - window.bubbleAnimation.setFPS(fps) - 设置帧率
 * - window.bubbleAnimation.setBubbleCount(count) - 设置气泡数量
 */
(function() {
    // ==================== 配置参数区域 ====================
    const BUBBLE_CONFIG = {
        // 气泡基础配置
        count: 15,              // 气泡数量
        size: 140,              // 气泡大小（像素）
        maxSpeed: 4,            // 最大移动速度

        // 鼠标交互配置
        mouseInfluenceRadius: 250,      // 鼠标影响半径（像素）
        mouseAttractionForce: 0.3,      // 鼠标吸引力强度（0-1）

        // 性能配置
        targetFPS: 30,          // 目标帧率
        resizeThrottle: 150,    // 窗口调整事件节流延迟（毫秒）

        // 颜色配置
        colors: [
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
        ]
    };
    // ==================== 配置参数区域结束 ====================

    // 等待DOM加载完成
    function initBubbleAnimation() {
        const container = document.getElementById('bubbleContainer');
        if (!container) {
            console.warn('未找到 #bubbleContainer 容器，气泡动画无法初始化');
            return;
        }

        const bubbles = [];
        let isAnimationRunning = true; // 气泡显示状态
        let isDestroyed = false;      // 是否已销毁
        let animationFrameId = null;
        let resizeTimeout = null; // resize 节流定时器

        // 帧率控制配置
        let targetFPS = BUBBLE_CONFIG.targetFPS;
        let frameInterval = 1000 / targetFPS;
        // 兼容性处理：performance.now() 降级方案
        let getTime = performance && performance.now ? function() { return performance.now(); } : function() { return Date.now(); };
        let lastFrameTime = getTime();

        // localStorage 存储键名
        const BUBBLE_VISIBLE_KEY = 'bubble_animation_visible';

        // 事件监听器引用（用于销毁时移除）
        let mouseMoveHandler = null;
        let resizeHandler = null;

        // 从 localStorage 读取初始状态（默认为 true，即显示）
        function getStoredVisibility() {
            try {
                var stored = localStorage.getItem(BUBBLE_VISIBLE_KEY);
                if (stored === null) {
                    // 首次访问：保存默认值 true 到 localStorage
                    saveVisibility(true);
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

        // 确保容器初始状态正确（首次访问或已关闭的情况）
        if (isAnimationRunning) {
            // 首次访问或已开启：确保容器显示
            container.style.display = 'block';
            isDestroyed = false;
        } else {
            // 已关闭：标记为已销毁，不创建气泡
            isDestroyed = true;
            container.style.display = 'none';
        }

        // 鼠标位置
        let mouseX = -1000;
        let mouseY = -1000;

        // 鼠标移动事件（保存引用以便销毁）
        mouseMoveHandler = function(e) {
            if (!isDestroyed) {
                mouseX = e.clientX;
                mouseY = e.clientY;
            }
        };
        document.addEventListener('mousemove', mouseMoveHandler);

        class Bubble {
            constructor(index, total, gridPositions) {
                this.element = document.createElement('div');
                this.element.className = 'bubble';

                // 使用配置的气泡大小
                this.size = BUBBLE_CONFIG.size;
                this.element.style.width = this.size + 'px';
                this.element.style.height = this.size + 'px';

                // 随机颜色（淡紫色系概率更高）
                var colorIndex = Math.floor(Math.random() * BUBBLE_CONFIG.colors.length);
                this.color = BUBBLE_CONFIG.colors[colorIndex];
                // Edge 兼容性：使用字符串拼接而不是模板字符串
                this.element.style.background = 'radial-gradient(circle at 30% 30%, ' + this.color.inner + ', ' + this.color.outer + ')';
                // 简化阴影层数，提升性能
                this.element.style.boxShadow =
                    'inset 0 0 20px rgba(255, 255, 255, 0.6), ' +
                    'inset -5px -5px 15px rgba(0, 0, 0, 0.1), ' +
                    '0 8px 20px rgba(0, 0, 0, 0.15), ' +
                    '0 0 25px ' + this.color.glow;

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
                // Edge 兼容性：使用字符串拼接而不是模板字符串
                this.element.style.transform = 'translate(' + this.x + 'px, ' + this.y + 'px)';
                // 添加浏览器前缀
                this.element.style.webkitTransform = this.element.style.transform;
                this.element.style.msTransform = this.element.style.transform;
            }

            move() {
                // 计算到鼠标的距离和方向
                var centerX = this.x + this.size / 2;
                var centerY = this.y + this.size / 2;
                var dx = mouseX - centerX;
                var dy = mouseY - centerY;
                var distance = Math.sqrt(dx * dx + dy * dy);

                // 如果在鼠标影响范围内，向鼠标方向加速
                if (distance < BUBBLE_CONFIG.mouseInfluenceRadius && distance > 0) {
                    var force = (1 - distance / BUBBLE_CONFIG.mouseInfluenceRadius) * BUBBLE_CONFIG.mouseAttractionForce;
                    this.vx += (dx / distance) * force;
                    this.vy += (dy / distance) * force;
                }

                // 限制最大速度
                var speed = Math.sqrt(this.vx * this.vx + this.vy * this.vy);
                if (speed > BUBBLE_CONFIG.maxSpeed) {
                    this.vx = (this.vx / speed) * BUBBLE_CONFIG.maxSpeed;
                    this.vy = (this.vy / speed) * BUBBLE_CONFIG.maxSpeed;
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

        // 检测气泡间碰撞（优化版 - 避免不必要的 Math.sqrt）
        function checkCollisions() {
            for (var i = 0; i < bubbles.length; i++) {
                for (var j = i + 1; j < bubbles.length; j++) {
                    var b1 = bubbles[i];
                    var b2 = bubbles[j];

                    var dx = (b1.x + b1.size / 2) - (b2.x + b2.size / 2);
                    var dy = (b1.y + b1.size / 2) - (b2.y + b2.size / 2);
                    var distanceSq = dx * dx + dy * dy; // 距离平方
                    var minDistance = (b1.size + b2.size) / 2;
                    var minDistanceSq = minDistance * minDistance; // 最小距离平方

                    // 先用距离平方判断，避免 Math.sqrt
                    if (distanceSq < minDistanceSq) {
                        // 只有确认碰撞时才计算实际距离
                        var distance = Math.sqrt(distanceSq);

                        // 碰撞响应 - 交换速度
                        var tempVx = b1.vx;
                        var tempVy = b1.vy;
                        b1.vx = b2.vx;
                        b1.vy = b2.vy;
                        b2.vx = tempVx;
                        b2.vy = tempVy;

                        // 分离重叠的气泡
                        var overlap = minDistance - distance;
                        var nx = dx / distance;
                        var ny = dy / distance;
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
            var positions = [];
            var cols = Math.ceil(Math.sqrt(count * (window.innerWidth / window.innerHeight)));
            var rows = Math.ceil(count / cols);
            var cellWidth = window.innerWidth / cols;
            var cellHeight = window.innerHeight / rows;

            for (var i = 0; i < count; i++) {
                var col = i % cols;
                var row = Math.floor(i / cols);

                // 在单元格内随机偏移，保持一定的分散性
                var offsetX = cellWidth * 0.1 + Math.random() * cellWidth * 0.8;
                var offsetY = cellHeight * 0.1 + Math.random() * cellHeight * 0.8;

                positions.push({
                    x: col * cellWidth + offsetX - 50,
                    y: row * cellHeight + offsetY - 50
                });
            }

            return positions;
        }

        // 初始化气泡（使用网格分布）- 只有未销毁时才创建
        if (!isDestroyed) {
            var gridPositions = generateGridPositions(BUBBLE_CONFIG.count);
            for (var i = 0; i < BUBBLE_CONFIG.count; i++) {
                bubbles.push(new Bubble(i, BUBBLE_CONFIG.count, gridPositions));
            }
            container.style.display = 'block';
        } else {
            container.style.display = 'none';
        }

        // 动画循环（带帧率控制）
        function animate(currentTime) {
            animationFrameId = requestAnimationFrame(animate);

            if (!isAnimationRunning) {
                return;
            }

            // 兼容性处理：确保 currentTime 存在
            if (currentTime == null) {
                currentTime = getTime();
            }

            var elapsed = currentTime - lastFrameTime;

            if (elapsed > frameInterval) {
                // 修正下一次帧的时间
                lastFrameTime = currentTime - (elapsed % frameInterval);

                // Edge 兼容性：使用传统 for 循环而不是 forEach
                for (var i = 0; i < bubbles.length; i++) {
                    bubbles[i].move();
                }
                checkCollisions();
            }
        }

        // 初始调用（不传递参数，让函数内部处理）
        animate(getTime());

        // 自动创建并插入开关按钮（灰色气泡+X图标）
        function createToggleButton() {
            // 避免重复创建
            if (document.getElementById('bubbleToggleBtn')) {
                return;
            }

            var toggleBtn = document.createElement('div');
            toggleBtn.id = 'bubbleToggleBtn';
            toggleBtn.className = 'bubble-toggle-btn';
            toggleBtn.innerHTML = '<span class="toggle-icon">×</span>';
            // 根据存储的状态设置按钮初始状态
            var initialVisibility = isAnimationRunning && !isDestroyed;
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

                // 如果未销毁，执行销毁；如果已销毁，执行重新初始化
                if (!isDestroyed) {
                    window.bubbleAnimation.destroy();
                    this.classList.add('bubble-hidden');
                    this.title = '显示气泡';
                } else {
                    window.bubbleAnimation.reinit();
                    this.classList.remove('bubble-hidden');
                    this.title = '隐藏气泡';
                }
            });
        }

        // 延迟创建按钮，确保body已加载
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', createToggleButton);
        } else {
            // 使用setTimeout确保在body之后插入
            setTimeout(createToggleButton, 0);
        }

        // 窗口大小改变时重新定位（带节流）
        resizeHandler = function() {
            if (isDestroyed) return;
            if (resizeTimeout) {
                clearTimeout(resizeTimeout);
            }
            resizeTimeout = setTimeout(function() {
                if (isDestroyed) return;
                // Edge 兼容性：使用传统 for 循环而不是 forEach
                for (var i = 0; i < bubbles.length; i++) {
                    var bubble = bubbles[i];
                    bubble.x = Math.min(bubble.x, window.innerWidth - bubble.size);
                    bubble.y = Math.min(bubble.y, window.innerHeight - bubble.size);
                    bubble.updatePosition();
                }
            }, BUBBLE_CONFIG.resizeThrottle);
        };
        window.addEventListener('resize', resizeHandler);

        // 暴露到全局，方便外部调用
        window.bubbleAnimation = {
            config: BUBBLE_CONFIG,  // 暴露配置对象
            bubbles,
            colors: BUBBLE_CONFIG.colors,  // 兼容旧版
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
                    var bubble = bubbles.pop();
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
                    var bubble = bubbles.pop();
                    if (bubble && bubble.element) {
                        bubble.element.remove();
                    }
                }
            },

            // 设置目标帧率（默认 60，可降低以节省性能）
            setFPS: function(fps) {
                targetFPS = Math.max(1, Math.min(120, fps)); // 限制在 1-120 FPS
                frameInterval = 1000 / targetFPS;
                return targetFPS;
            },

            // 获取当前帧率设置
            getFPS: function() {
                return targetFPS;
            },

            // 完全销毁气泡动画，释放所有资源
            destroy: function() {
                if (isDestroyed) return;

                // 停止动画循环
                if (animationFrameId) {
                    cancelAnimationFrame(animationFrameId);
                    animationFrameId = null;
                }

                // 清除 resize 定时器
                if (resizeTimeout) {
                    clearTimeout(resizeTimeout);
                    resizeTimeout = null;
                }

                // 移除所有气泡 DOM 元素
                // Edge 兼容性：使用传统 for 循环而不是 forEach
                for (var i = 0; i < bubbles.length; i++) {
                    var bubble = bubbles[i];
                    if (bubble && bubble.element) {
                        bubble.element.remove();
                    }
                }
                bubbles.length = 0; // 清空数组

                // 移除事件监听器
                if (mouseMoveHandler) {
                    document.removeEventListener('mousemove', mouseMoveHandler);
                    mouseMoveHandler = null;
                }
                if (resizeHandler) {
                    window.removeEventListener('resize', resizeHandler);
                    resizeHandler = null;
                }

                // 注意：不移除开关按钮，保留按钮供重新初始化使用

                // 隐藏容器
                container.style.display = 'none';

                // 标记为已销毁
                isDestroyed = true;
                isAnimationRunning = false;
                this.isVisible = false;

                // 保存销毁状态到 localStorage（保存为 false，表示已关闭）
                saveVisibility(false);

                return true;
            },

            // 重新初始化气泡动画
            reinit: function() {
                if (!isDestroyed) {
                    console.warn('气泡动画未销毁，无需重新初始化');
                    return false;
                }

                // 重置状态 - 重新初始化意味着要启用气泡，所以强制设为 true
                isDestroyed = false;
                isAnimationRunning = true;

                // 显示容器
                container.style.display = 'block';

                // 重新生成气泡
                var gridPositions = generateGridPositions(BUBBLE_CONFIG.count);
                for (var i = 0; i < BUBBLE_CONFIG.count; i++) {
                    bubbles.push(new Bubble(i, BUBBLE_CONFIG.count, gridPositions));
                }

                // 重新注册事件监听器
                if (!mouseMoveHandler) {
                    mouseMoveHandler = function(e) {
                        if (!isDestroyed) {
                            mouseX = e.clientX;
                            mouseY = e.clientY;
                        }
                    };
                    document.addEventListener('mousemove', mouseMoveHandler);
                }

                if (!resizeHandler) {
                    resizeHandler = function() {
                        if (isDestroyed) return;
                        if (resizeTimeout) {
                            clearTimeout(resizeTimeout);
                        }
                        resizeTimeout = setTimeout(function() {
                            if (isDestroyed) return;
                            // Edge 兼容性：使用传统 for 循环而不是 forEach
                            for (var i = 0; i < bubbles.length; i++) {
                                var bubble = bubbles[i];
                                bubble.x = Math.min(bubble.x, window.innerWidth - bubble.size);
                                bubble.y = Math.min(bubble.y, window.innerHeight - bubble.size);
                                bubble.updatePosition();
                            }
                        }, BUBBLE_CONFIG.resizeThrottle);
                    };
                    window.addEventListener('resize', resizeHandler);
                }

                // 更新开关按钮状态（按钮已存在，无需重新创建）
                var toggleBtn = document.getElementById('bubbleToggleBtn');
                if (toggleBtn) {
                    toggleBtn.classList.remove('bubble-hidden');
                    toggleBtn.title = '隐藏气泡';
                }

                // 重新启动动画
                animate();

                this.isVisible = isAnimationRunning;

                // 保存重新初始化状态到 localStorage（保存为 true，表示已开启）
                saveVisibility(isAnimationRunning);

                return true;
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
