/* =============================================
   供应商管理系统 - JavaScript文件
   Vendor Portal - JavaScript
   ============================================= */

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化函数
    initVendorPortal();
    
    // 设置库存条的实际宽度
    setupStockLevels();
});

// 初始化供应商门户
function initVendorPortal() {
    // 移动端菜单切换
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
            
            // 更新图标
            const icon = this.querySelector('i');
            if (sidebar.classList.contains('active')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
    }
    
    // 菜单项点击效果
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.addEventListener('click', function() {
            // 移除所有菜单项的active类
            menuItems.forEach(i => i.classList.remove('active'));
            
            // 为当前点击的菜单项添加active类
            this.classList.add('active');
            
            // 如果是移动端，点击菜单后关闭侧边栏
            if (window.innerWidth <= 991) {
                sidebar.classList.remove('active');
                menuToggle.querySelector('i').classList.remove('fa-times');
                menuToggle.querySelector('i').classList.add('fa-bars');
            }
            
            // 在实际应用中，这里会加载对应的页面内容
            const menuText = this.querySelector('span').textContent;
            console.log('切换到:', menuText);
            
            // 更新页面标题
            if (menuText !== '仪表板') {
                document.querySelector('.dashboard-title').textContent = menuText;
            } else {
                document.querySelector('.dashboard-title').textContent = '供应商仪表板';
            }
        });
    });
    
    // 搜索功能
    const searchInput = document.querySelector('.search-bar input');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.trim().toLowerCase();
            
            if (searchTerm.length > 1) {
                // 在实际应用中，这里会调用搜索API
                console.log('搜索关键词:', searchTerm);
                
                // 这里可以添加搜索结果的显示逻辑
                simulateSearch(searchTerm);
            }
        });
        
        // 防抖处理搜索输入
        searchInput.addEventListener('keyup', debounce(function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        }, 500));
    }
    
    // 通知点击
    const notification = document.querySelector('.notification');
    if (notification) {
        notification.addEventListener('click', function() {
            alert('您有3条未读通知\n1. 新订单待处理\n2. 库存预警提醒\n3. 系统维护通知');
            
            // 清除通知徽章
            const badge = this.querySelector('.notification-badge');
            if (badge) {
                badge.style.display = 'none';
            }
        });
    }
    
    // 查看订单按钮
    const viewButtons = document.querySelectorAll('.btn-view');
    viewButtons.forEach(button => {
        button.addEventListener('click', function() {
            const orderId = this.closest('tr').querySelector('td:first-child').textContent;
            alert(`查看订单: ${orderId}\n详细信息将在实际应用中显示`);
        });
    });
    
    // 补货按钮
    const restockButtons = document.querySelectorAll('.btn-small');
    restockButtons.forEach(button => {
        button.addEventListener('click', function() {
            const productName = this.closest('.alert-item').querySelector('.product-name').textContent;
            const quantity = prompt(`为 ${productName} 补货，请输入数量:`, "50");
            
            if (quantity && !isNaN(quantity) && quantity > 0) {
                alert(`已提交补货申请: ${productName} × ${quantity} 件`);
                
                // 模拟更新库存
                const stockLevel = this.closest('.alert-item').querySelector('.stock-fill');
                const currentWidth = parseInt(stockLevel.style.width) || 12;
                const newWidth = Math.min(currentWidth + 20, 100);
                stockLevel.style.width = `${newWidth}%`;
                
                // 更新颜色
                if (newWidth > 30) {
                    stockLevel.className = 'stock-fill stock-medium';
                }
                if (newWidth > 70) {
                    stockLevel.className = 'stock-fill stock-good';
                }
                
                // 更新库存数字
                const stockText = this.closest('.alert-item').querySelector('.product-stock');
                const match = stockText.textContent.match(/(\d+)\s*\/\s*(\d+)/);
                if (match) {
                    const current = parseInt(match[1]) + parseInt(quantity);
                    const total = parseInt(match[2]);
                    stockText.textContent = `库存: ${current} / ${total}`;
                }
            }
        });
    });
    
    // 模拟数据更新
    startDataSimulation();
}

// 设置库存条宽度
function setupStockLevels() {
    const stockBars = document.querySelectorAll('.stock-fill');
    
    // 根据产品设置不同的库存水平
    stockBars.forEach((bar, index) => {
        let width;
        if (index === 0) { // 智能手表 X200
            width = 12;
            bar.className = 'stock-fill stock-low';
        } else if (index === 1) { // 无线耳机 Pro
            width = 17;
            bar.className = 'stock-fill stock-low';
        } else if (index === 2) { // 手机充电器
            width = 22.5;
            bar.className = 'stock-fill stock-medium';
        }
        
        bar.style.width = `${width}%`;
    });
}

// 模拟搜索功能
function simulateSearch(term) {
    // 在实际应用中，这里会显示搜索结果
    // 这里只是一个模拟
    const tableRows = document.querySelectorAll('.order-table tbody tr');
    
    tableRows.forEach(row => {
        const rowText = row.textContent.toLowerCase();
        if (rowText.includes(term)) {
            row.style.backgroundColor = 'rgba(52, 152, 219, 0.1)';
        } else {
            row.style.backgroundColor = '';
        }
    });
}

// 执行搜索
function performSearch(term) {
    if (term.trim() === '') return;
    
    alert(`执行搜索: "${term}"\n在实际应用中，这里会显示搜索结果页面`);
    
    // 清空搜索框
    document.querySelector('.search-bar input').value = '';
}

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            func.apply(context, args);
        }, wait);
    };
}

// 模拟数据更新
function startDataSimulation() {
    // 每30秒更新一次仪表板数据
    setInterval(updateDashboardData, 30000);
    
    // 每60秒更新一次订单状态
    setInterval(updateOrderStatus, 60000);
}

// 更新仪表板数据
function updateDashboardData() {
    const cards = document.querySelectorAll('.card');
    
    if (cards.length >= 4) {
        // 更新今日订单
        const todayOrders = cards[0].querySelector('.card-value');
        const todayChange = cards[0].querySelector('.card-change');
        const currentOrders = parseInt(todayOrders.textContent);
        const randomChange = Math.floor(Math.random() * 6) - 1; // -1 到 4
        
        todayOrders.textContent = Math.max(0, currentOrders + randomChange);
        todayChange.textContent = randomChange >= 0 ? `+${randomChange} 个订单` : `${randomChange} 个订单`;
        todayChange.className = `card-change ${randomChange >= 0 ? 'positive' : 'negative'}`;
        
        // 更新待处理订单
        const pendingOrders = cards[1].querySelector('.card-value');
        const pendingChange = cards[1].querySelector('.card-change');
        const currentPending = parseInt(pendingOrders.textContent);
        const randomPendingChange = Math.floor(Math.random() * 4) - 1; // -1 到 2
        
        pendingOrders.textContent = Math.max(0, currentPending + randomPendingChange);
        pendingChange.textContent = randomPendingChange >= 0 ? `+${randomPendingChange} 个待处理` : `${randomPendingChange} 个待处理`;
        pendingChange.className = `card-change ${randomPendingChange >= 0 ? 'negative' : 'positive'}`;
        
        // 更新销售额
        const salesValue = cards[2].querySelector('.card-value');
        const salesChange = cards[2].querySelector('.card-change');
        const currentSales = parseInt(salesValue.textContent.replace('$', '').replace(',', ''));
        const salesIncrease = Math.floor(currentSales * 0.02); // 2% 增长
        
        salesValue.textContent = `$${(currentSales + salesIncrease).toLocaleString()}`;
        salesChange.textContent = `+${(salesIncrease / currentSales * 100).toFixed(1)}% 增长`;
        salesChange.className = 'card-change positive';
    }
}

// 更新订单状态
function updateOrderStatus() {
    const statusBadges = document.querySelectorAll('.status-badge');
    
    statusBadges.forEach(badge => {
        // 随机更新一些订单状态
        if (Math.random() > 0.7) {
            const currentStatus = badge.textContent.trim();
            let newStatus, newClass;
            
            if (currentStatus === '待处理') {
                newStatus = '处理中';
                newClass = 'status-processing';
            } else if (currentStatus === '处理中') {
                newStatus = '已发货';
                newClass = 'status-shipped';
            } else if (currentStatus === '已发货') {
                newStatus = '已交付';
                newClass = 'status-delivered';
            }
            
            if (newStatus) {
                badge.textContent = newStatus;
                badge.className = `status-badge ${newClass}`;
                
                // 在实际应用中，这里会发送状态更新到服务器
                console.log(`订单状态更新: ${currentStatus} -> ${newStatus}`);
            }
        }
    });
}

// 窗口大小变化时调整布局
window.addEventListener('resize', function() {
    const sidebar = document.getElementById('sidebar');
    const menuToggle = document.getElementById('menuToggle');
    
    // 如果窗口变大，且当前是移动端菜单状态，则恢复正常
    if (window.innerWidth > 991 && sidebar.classList.contains('active')) {
        sidebar.classList.remove('active');
        if (menuToggle) {
            menuToggle.querySelector('i').classList.remove('fa-times');
            menuToggle.querySelector('i').classList.add('fa-bars');
        }
    }
    
    // 如果窗口变小，确保主内容区没有左边距
    if (window.innerWidth <= 991) {
        document.querySelector('.main-content').style.marginLeft = '0';
    } else {
        document.querySelector('.main-content').style.marginLeft = 'var(--sidebar-width)';
    }
});

// ========== 新增产品功能（无动画版本） ==========

// API端点
const API_ENDPOINTS = {
    CREATE_PRODUCT: '/products',  // POST
    LIST_PRODUCTS: '/products',   // GET
    GET_PRODUCT: (id) => `/products/${id}`,
    UPDATE_PRODUCT: (id) => `/products/${id}`,
    UPDATE_STATUS: (id) => `/products/${id}/status`
};

// 显示/隐藏表单
document.getElementById('toggleFormBtn').addEventListener('click', function() {
    const formCard = document.getElementById('productFormCard');
    
    if (formCard.style.display === 'none') {
        formCard.style.display = 'block';
        this.innerHTML = '<i class="fas fa-minus"></i> 隐藏表单';
    } else {
        formCard.style.display = 'none';
        this.innerHTML = '<i class="fas fa-plus"></i> 显示表单';
    }
});

// 快速新增卡片点击
document.getElementById('quickAddProduct').addEventListener('click', function() {
    const formCard = document.getElementById('productFormCard');
    const toggleBtn = document.getElementById('toggleFormBtn');
    
    if (formCard.style.display !== 'block') {
        formCard.style.display = 'block';
        toggleBtn.innerHTML = '<i class="fas fa-minus"></i> 隐藏表单';
    }
});

// 重置表单
document.getElementById('resetFormBtn').addEventListener('click', function() {
    document.getElementById('productForm').reset();
    document.getElementById('formFeedback').style.display = 'none';
});

// 表单提交 - 调用您的 POST /products API
document.getElementById('productForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    // 收集表单数据
    const productData = {
        name: document.getElementById('productName').value.trim(),
        sku: document.getElementById('productSku').value.trim(),
        price: parseFloat(document.getElementById('productPrice').value) || 0,
        stock: parseInt(document.getElementById('productStock').value) || 0,
        category: document.getElementById('productCategory').value,
        description: document.getElementById('productDescription').value.trim()
    };
    
    // 简单验证
    if (!productData.name || !productData.sku) {
        alert('产品名称和SKU编码是必填项');
        return;
    }
    
    // 显示加载状态
    const submitBtn = this.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 创建中...';
    
    try {
        // 调用您的 POST /products API
        const response = await fetch(API_ENDPOINTS.CREATE_PRODUCT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(productData)
        });
        
        if (response.ok) {
            const result = await response.json();
            
            // 显示成功消息
            document.getElementById('feedbackMessage').textContent = 
                `产品 "${productData.name}" 创建成功！SKU: ${productData.sku}`;
            document.getElementById('formFeedback').style.display = 'block';
            
            // 更新UI
            updateStockAlertCount(productData);
            updateProductCountBadge();
            
            // 重置表单
            this.reset();
            
            // 5秒后隐藏反馈
            setTimeout(() => {
                document.getElementById('formFeedback').style.display = 'none';
            }, 5000);
            
        } else {
            throw new Error(`HTTP错误: ${response.status}`);
        }
        
    } catch (error) {
        console.error('创建产品失败:', error);
        alert('创建产品失败，请重试');
    } finally {
        // 恢复按钮状态
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-save"></i> 创建产品';
    }
});

// 更新库存预警计数
function updateStockAlertCount(productData) {
    // 更新低库存卡片
    const lowStockCard = document.querySelector('.card:nth-child(4) .card-value');
    if (lowStockCard) {
        const currentCount = parseInt(lowStockCard.textContent) || 0;
        lowStockCard.textContent = currentCount + 1;
    }
    
    // 在库存预警列表中添加新产品
    const inventoryAlert = document.querySelector('.inventory-alert');
    if (inventoryAlert) {
        const newAlert = document.createElement('div');
        newAlert.className = 'alert-item';
        newAlert.innerHTML = `
            <div class="product-info">
                <div class="product-img">
                    <i class="fas fa-box"></i>
                </div>
                <div>
                    <div style="font-weight: 600;">${productData.name}</div>
                    <div style="font-size: 0.9rem; color: #666;">库存: ${productData.stock}</div>
                    <div class="stock-level">
                        <div class="stock-fill ${productData.stock > 50 ? 'stock-good' : productData.stock > 20 ? 'stock-medium' : 'stock-low'}" 
                             style="width: ${Math.min(productData.stock, 100)}%"></div>
                    </div>
                </div>
            </div>
            <button class="btn btn-small" onclick="viewProductDetail('${productData.sku}')">
                <i class="fas fa-eye"></i> 查看
            </button>
        `;
        
        if (inventoryAlert.firstChild) {
            inventoryAlert.insertBefore(newAlert, inventoryAlert.firstChild);
        } else {
            inventoryAlert.appendChild(newAlert);
        }
    }
}

// 更新产品菜单徽章
function updateProductCountBadge() {
    const badge = document.getElementById('productCountBadge');
    if (badge) {
        const currentCount = parseInt(badge.textContent) || 0;
        badge.textContent = currentCount + 1;
    }
}

// 查看产品详情（示例）
function viewProductDetail(sku) {
    alert(`查看产品: ${sku}\n在实际应用中，这里会调用 GET /products/{id} API`);
}

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    // 检查URL参数
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('action') === 'add-product') {
        document.getElementById('productFormCard').style.display = 'block';
        document.getElementById('toggleFormBtn').innerHTML = '<i class="fas fa-minus"></i> 隐藏表单';
    }
});