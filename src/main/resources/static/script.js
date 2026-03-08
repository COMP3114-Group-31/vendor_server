// ==========================================
// 全局配置与状态变量
// ==========================================
const API_ENDPOINTS = {
    DASHBOARD_SUMMARY: '/dashboard/summary',
    CREATE_PRODUCT: '/products',
    LIST_PRODUCTS: '/products',
    GET_PRODUCT: (id) => `/products/${id}`,
    UPDATE_PRODUCT: (id) => `/products/${id}`,
    UPDATE_STATUS: (id) => `/products/${id}/status`,
    CREATE_ORDER: '/orders',
    LIST_ORDERS: '/orders',
    GET_ORDER: (id) => `/orders/${id}`,
    UPDATE_ORDER: (id) => `/orders/${id}`,
    DELETE_ORDER: (id) => `/orders/${id}`
};

let currentEditingProductId = null;
let currentEditingOrderId = null; 

let currentExistingCoverUrl = '';
let currentNewCoverFile = null;
let currentExistingMedia = []; 
let currentNewFiles = []; // 数据结构: { file: File对象, url: '内存预览地址' }
let currentDashboardSearch = '';
let currentProductSearch = '';
let currentOrderSearch = '';

// ==========================================
// 初始化
// ==========================================
document.addEventListener('DOMContentLoaded', function () {
    initVendorPortal();
    initProductManagement();
    initOrderManagement(); 
    loadDashboardSummary();
});

// ==========================================
// 1. 基础框架与侧边栏逻辑
// ==========================================
function initVendorPortal() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const globalSearchInput = document.getElementById('globalSearchInput');
    const updateGlobalSearchPlaceholder = (targetId) => {
        if (!globalSearchInput) return;
        if (targetId === 'productSection') {
            globalSearchInput.placeholder = '搜索产品名称...';
            return;
        }
        if (targetId === 'orderSection') {
            globalSearchInput.placeholder = '搜索订单号、客户或状态...';
            return;
        }
        globalSearchInput.placeholder = '搜索订单号或客户...';
    };

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function () {
            sidebar.classList.toggle('active');
            const icon = this.querySelector('i');
            if (!icon) return;
            if (sidebar.classList.contains('active')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
    }

    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach((item) => {
        item.addEventListener('click', function () {
            menuItems.forEach((i) => i.classList.remove('active'));
            this.classList.add('active');

            if (window.innerWidth <= 991 && sidebar && menuToggle) {
                sidebar.classList.remove('active');
                const icon = menuToggle.querySelector('i');
                if (icon) {
                    icon.classList.remove('fa-times');
                    icon.classList.add('fa-bars');
                }
            }

            const menuTextNode = this.querySelector('span');
            const title = document.querySelector('.dashboard-title');
            if (menuTextNode && title) {
                title.textContent = menuTextNode.textContent;
            }

            const targetId = this.getAttribute('data-target');
            if (targetId) {
                document.querySelectorAll('.content-section').forEach(section => {
                    section.style.display = 'none'; 
                });
                const targetSection = document.getElementById(targetId);
                if (targetSection) {
                    targetSection.style.display = 'block'; 
                }

                if (globalSearchInput) {
                    globalSearchInput.value = '';
                }
                updateGlobalSearchPlaceholder(targetId);
                currentDashboardSearch = '';
                currentProductSearch = '';
                currentOrderSearch = '';

                if (targetId === 'dashboardSection') {
                    loadDashboardSummary();
                }
                if (targetId === 'productSection') {
                    loadProductList();
                }
                if (targetId === 'orderSection') {
                    loadOrderList();
                }
            }
        });
    });

    const cardTodayOrders = document.getElementById('cardTodayOrders');
    const cardPendingOrders = document.getElementById('cardPendingOrders');
    const orderMenu = document.querySelector('.menu-item[data-target="orderSection"]');

    if (cardTodayOrders && orderMenu) cardTodayOrders.addEventListener('click', () => orderMenu.click());
    if (cardPendingOrders && orderMenu) cardPendingOrders.addEventListener('click', () => orderMenu.click());
    updateGlobalSearchPlaceholder(document.querySelector('.menu-item.active')?.getAttribute('data-target'));

    if (globalSearchInput) {
        globalSearchInput.addEventListener('input', function () {
            const keyword = (this.value || '').trim();
            const activeTarget = document.querySelector('.menu-item.active')?.getAttribute('data-target');

            if (activeTarget === 'productSection') {
                currentProductSearch = keyword;
                loadProductList();
                return;
            }

            if (activeTarget === 'orderSection') {
                currentOrderSearch = keyword;
                loadOrderList();
                return;
            }

            if (activeTarget === 'dashboardSection' || !activeTarget) {
                currentDashboardSearch = keyword;
                loadDashboardSummary();
            }
        });
    }

}

async function loadDashboardSummary() {
    const recentOrdersBody = document.getElementById('dashboardRecentOrdersBody');

    try {
        const params = new URLSearchParams();
        if (currentDashboardSearch) {
            params.set('search', currentDashboardSearch);
        }
        const url = params.toString() ? `${API_ENDPOINTS.DASHBOARD_SUMMARY}?${params.toString()}` : API_ENDPOINTS.DASHBOARD_SUMMARY;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const data = await response.json();
        const todayOrders = Number(data.today_orders || 0);
        const pendingOrders = Number(data.pending_orders || 0);
        const monthSales = Number(data.month_sales || 0);
        const productCount = Number(data.product_count || 0);
        const recentOrders = Array.isArray(data.recent_orders) ? data.recent_orders : [];

        const setText = (id, text) => {
            const el = document.getElementById(id);
            if (el) el.textContent = text;
        };

        setText('dashboardTodayOrdersValue', String(todayOrders));
        setText('dashboardTodayOrdersHint', `今日共 ${todayOrders} 笔订单`);
        setText('dashboardPendingOrdersValue', String(pendingOrders));
        setText('dashboardPendingOrdersHint', `当前有 ${pendingOrders} 笔待处理`);
        setText('dashboardMonthSalesValue', `$${monthSales.toFixed(2)}`);
        setText('dashboardMonthSalesHint', '本月订单金额汇总');
        setText('dashboardProductCountValue', String(productCount));

        renderDashboardRecentOrders(recentOrders);
    } catch (error) {
        if (recentOrdersBody) {
            recentOrdersBody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#e74c3c;padding:20px;">仪表盘数据加载失败</td></tr>';
        }
    }
}

function renderDashboardRecentOrders(orders) {
    const tbody = document.getElementById('dashboardRecentOrdersBody');
    if (!tbody) return;

    if (!orders.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#7f8c8d;padding:20px;">暂无最近订单</td></tr>';
        return;
    }

    tbody.innerHTML = orders.map((o) => {
        const id = o.order_id || o.id || '';
        let statusClass = 'status-pending';
        if (o.status === '处理中') statusClass = 'status-processing';
        if (o.status === '已发货') statusClass = 'status-shipped';
        if (o.status === '已交付') statusClass = 'status-delivered';

        return `<tr>
            <td>#${id}</td>
            <td>${escapeHtml(o.customer_name || '')}</td>
            <td>${escapeHtml(o.created_at ? new Date(o.created_at).toLocaleDateString() : '-')}</td>
            <td>$${Number(o.total_amount || 0).toFixed(2)}</td>
            <td><span class="status-badge ${statusClass}">${escapeHtml(o.status || '待处理')}</span></td>
            <td><button class="btn-view" data-dashboard-order-id="${id}"><i class="fas fa-eye"></i> 查看</button></td>
        </tr>`;
    }).join('');

    tbody.querySelectorAll('button[data-dashboard-order-id]').forEach((btn) => {
        btn.addEventListener('click', () => {
            document.querySelector('.menu-item[data-target="orderSection"]')?.click();
            handleEditOrder(btn.getAttribute('data-dashboard-order-id'));
        });
    });
}

// ==========================================
// 2. 产品管理模块逻辑
// ==========================================
function initProductManagement() {
    const toggleFormBtn = document.getElementById('toggleFormBtn');
    if (toggleFormBtn) {
        toggleFormBtn.addEventListener('click', function () {
            const formCard = document.getElementById('productFormCard');
            if (!formCard) return;
            const open = formCard.style.display === 'none';
            formCard.style.display = open ? 'block' : 'none';
            this.innerHTML = open ? '<i class="fas fa-minus"></i> 隐藏表单' : '<i class="fas fa-plus"></i> 显示表单';
        });
    }

    const quickAddProduct = document.getElementById('quickAddProduct');
    if (quickAddProduct) {
        quickAddProduct.addEventListener('click', function () {
            const productMenu = document.getElementById('productsMenu');
            if (productMenu) productMenu.click();

            const formCard = document.getElementById('productFormCard');
            const toggleBtn = document.getElementById('toggleFormBtn');
            if (formCard) formCard.style.display = 'block';
            if (toggleBtn) toggleBtn.innerHTML = '<i class="fas fa-minus"></i> 隐藏表单';
            
            setFormModeForCreate();
            if (formCard) formCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }

    document.getElementById('resetFormBtn')?.addEventListener('click', setFormModeForCreate);
    document.getElementById('cancelEditBtn')?.addEventListener('click', setFormModeForCreate);

    const productForm = document.getElementById('productForm');
    if (productForm) {
        productForm.addEventListener('submit', handleProductFormSubmit);
    }

    document.getElementById('refreshProductListBtn')?.addEventListener('click', loadProductList);

    // 监听表格里的操作（编辑、删除产品、快捷删除单张图片）
    const productTableBody = document.getElementById('productTableBody');
    if (productTableBody) {
        productTableBody.addEventListener('click', async function (e) {
            // 1. 如果点击的是某张图片上的 "×" 删除按钮
            const deleteMediaBtn = e.target.closest('.btn-delete-media');
            if (deleteMediaBtn) {
                e.stopPropagation();
                const productId = deleteMediaBtn.getAttribute('data-product-id');
                const mediaId = deleteMediaBtn.getAttribute('data-media-id');
                const url = deleteMediaBtn.getAttribute('data-url');
                await handleDeleteProductMedia(productId, mediaId, url);
                return;
            }

            // 2. 常规的编辑或删除产品
            const btn = e.target.closest('button[data-action]');
            if (!btn) return;
            const action = btn.getAttribute('data-action');
            const id = btn.getAttribute('data-id');
            if (!id) return;
            if (action === 'edit') await handleEditProduct(id);
            if (action === 'delete') await handleDeleteProduct(id);
        });
    }

    // 监听多文件上传选择 (已修复内存泄露问题)
    const imageInput = document.getElementById('productImage');
    if (imageInput) {
        imageInput.addEventListener('change', function(e) {
            const files = Array.from(e.target.files);
            files.forEach(file => {
                // 提前生成 URL 并缓存，方便后续精准释放内存
                currentNewFiles.push({
                    file: file,
                    url: URL.createObjectURL(file)
                });
            });
            renderImagePreviews(); 
            e.target.value = ''; // 清空 input 允许二次选择同一张
        });
    }

    const coverInput = document.getElementById('productCoverImage');
    if (coverInput) {
        coverInput.addEventListener('change', function (e) {
            const file = e.target.files?.[0];
            if (!file) return;

            if (currentNewCoverFile?.url) {
                URL.revokeObjectURL(currentNewCoverFile.url);
            }

            currentNewCoverFile = {
                file: file,
                url: URL.createObjectURL(file)
            };
            renderCoverPreview();
            e.target.value = '';
        });
    }

    setFormModeForCreate();
    loadProductList();
}

function renderCoverPreview() {
    const container = document.getElementById('coverPreviewContainer');
    const list = document.getElementById('coverPreviewList');
    if (!container || !list) return;

    list.innerHTML = '';
    const coverUrl = currentNewCoverFile?.url || currentExistingCoverUrl;
    container.style.display = coverUrl ? 'block' : 'none';
    if (!coverUrl) return;

    const canDelete = !!currentNewCoverFile;
    list.appendChild(createImageWrapper(coverUrl, () => {
        if (currentNewCoverFile?.url) {
            URL.revokeObjectURL(currentNewCoverFile.url);
        }
        currentNewCoverFile = null;
        renderCoverPreview();
    }, canDelete));
}

function renderImagePreviews() {
    const container = document.getElementById('imagePreviewContainer');
    const list = document.getElementById('imagePreviewList');
    if (!container || !list) return;

    list.innerHTML = '';
    const hasImages = currentExistingMedia.length > 0 || currentNewFiles.length > 0;
    container.style.display = hasImages ? 'block' : 'none';

    // 渲染后端已存在的图片
    currentExistingMedia.forEach((media, index) => {
        const wrap = createImageWrapper(media.url, () => {
            currentExistingMedia.splice(index, 1); 
            renderImagePreviews(); 
        });
        list.appendChild(wrap);
    });

    // 渲染新选择的文件
    currentNewFiles.forEach((item, index) => {
        const wrap = createImageWrapper(item.url, () => {
            URL.revokeObjectURL(item.url); // 销毁内存地址，防止内存泄露
            currentNewFiles.splice(index, 1);
            renderImagePreviews();
        });
        list.appendChild(wrap);
    });
}

// 创建带删除按钮的方块组件
function createImageWrapper(src, onDelete, showDelete = true) {
    const div = document.createElement('div');
    div.style.position = 'relative';
    div.style.width = '80px';
    div.style.height = '80px';
    
    const img = document.createElement('img');
    img.src = src;
    img.style.width = '100%';
    img.style.height = '100%';
    img.style.objectFit = 'cover';
    img.style.borderRadius = '5px';
    img.style.border = '1px solid #ddd';

    div.appendChild(img);
    if (showDelete) {
        const delBtn = document.createElement('button');
        delBtn.innerHTML = '<i class="fas fa-times"></i>';
        delBtn.style.position = 'absolute';
        delBtn.style.top = '-6px';
        delBtn.style.right = '-6px';
        delBtn.style.background = '#e74c3c';
        delBtn.style.color = 'white';
        delBtn.style.border = 'none';
        delBtn.style.borderRadius = '50%';
        delBtn.style.width = '20px';
        delBtn.style.height = '20px';
        delBtn.style.cursor = 'pointer';
        delBtn.style.display = 'flex';
        delBtn.style.alignItems = 'center';
        delBtn.style.justifyContent = 'center';
        delBtn.style.fontSize = '12px';
        delBtn.style.boxShadow = '0 2px 4px rgba(0,0,0,0.2)';
        delBtn.onclick = (e) => { e.preventDefault(); onDelete(); };
        div.appendChild(delBtn);
    }
    return div;
}

async function uploadCoverByBackend(productId, file) {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch(`/products/${productId}/images/cover`, {
        method: 'POST',
        body: formData
    });
    const data = await response.json().catch(() => null);
    if (!response.ok) throw new Error(getErrorMessageFromResponse(data, '封面图上传失败'));
    return data;
}

async function uploadDetailImagesByBackend(productId, files) {
    try {
        const formData = new FormData();
        files.forEach(file => { formData.append('files', file); });
        const response = await fetch(`/products/${productId}/images?set_first_as_main=false`, {
            method: 'POST',
            body: formData
        });
        const data = await response.json().catch(() => null);
        if (!response.ok) throw new Error(getErrorMessageFromResponse(data, '图片上传失败'));
        return data;
    } catch (error) {
        throw error;
    }
}

async function handleProductFormSubmit(e) {
    e.preventDefault();
    const name = (document.getElementById('productName')?.value || '').trim();
    if (!name) return showNotification('产品名称是必填项', 'error');

    const submitBtn = document.querySelector('#productForm button[type="submit"]');
    const originalText = submitBtn?.innerHTML || '';
    if (submitBtn) { submitBtn.disabled = true; submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 提交中...'; }

    try {
        const isEdit = currentEditingProductId !== null;
        const endpoint = isEdit ? API_ENDPOINTS.UPDATE_PRODUCT(currentEditingProductId) : API_ENDPOINTS.CREATE_PRODUCT;
        
        const payload = {
            name: name,
            name_cn: (document.getElementById('productNameCn')?.value || '').trim(),
            price: parseFloat(document.getElementById('productPrice')?.value) || 0,
            category: document.getElementById('productCategory')?.value || '',
            description: (document.getElementById('productDescription')?.value || '').trim(),
            description_cn: (document.getElementById('productDescriptionCn')?.value || '').trim()
        };

        const response = await fetch(endpoint, {
            method: isEdit ? 'PATCH' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => null);
            throw new Error(getErrorMessageFromResponse(errorData, '提交失败'));
        }

        const responseData = await response.json().catch(() => ({}));
        const productId = isEdit ? currentEditingProductId : responseData?.product_id;

        if (currentNewCoverFile?.file && productId) {
            if (submitBtn) submitBtn.innerHTML = '<i class="fas fa-image"></i> 正在上传封面...';
            await uploadCoverByBackend(productId, currentNewCoverFile.file);
        } else if (currentNewCoverFile?.file && !productId) {
            throw new Error('产品已保存，但未获取到 product_id，无法上传封面图');
        }

        const filesToUpload = currentNewFiles.map(item => item.file);

        if (filesToUpload.length > 0 && productId) {
            if (submitBtn) submitBtn.innerHTML = '<i class="fas fa-images"></i> 正在上传详情图...';
            await uploadDetailImagesByBackend(productId, filesToUpload);
        } else if (filesToUpload.length > 0 && !productId) {
            throw new Error('产品已保存，但未获取到 product_id，无法上传详情图');
        }

        showNotification(isEdit ? '产品更新成功' : '产品创建成功', 'success');
        setFormModeForCreate();
        await loadProductList();
    } catch (error) {
        console.error(error);
        showNotification(error.message || '操作失败，请重试', 'error');
    } finally {
        if (submitBtn) { submitBtn.disabled = false; submitBtn.innerHTML = originalText; }
    }
}

function setFormModeForCreate() {
    currentEditingProductId = null;
    currentExistingCoverUrl = '';
    currentExistingMedia = []; 

    if (currentNewCoverFile?.url) {
        URL.revokeObjectURL(currentNewCoverFile.url);
    }
    currentNewCoverFile = null;
    
    if (currentNewFiles.length > 0) {
        currentNewFiles.forEach(item => URL.revokeObjectURL(item.url));
    }
    currentNewFiles = [];      

    document.getElementById('productForm')?.reset();
    
    const submitBtn = document.getElementById('productSubmitBtn');
    if (submitBtn) { submitBtn.innerHTML = '<i class="fas fa-save"></i> 创建产品'; submitBtn.style.background = '#27ae60'; }
    const cancelBtn = document.getElementById('cancelEditBtn');
    if (cancelBtn) cancelBtn.style.display = 'none';

    renderCoverPreview();
    renderImagePreviews();
}

function setFormModeForEdit(product) {
    currentEditingProductId = product.product_id || product.id;
    document.getElementById('productFormCard').style.display = 'block';
    document.getElementById('toggleFormBtn').innerHTML = '<i class="fas fa-minus"></i> 隐藏表单';

    const setVal = (id, val) => { const el = document.getElementById(id); if (el) el.value = val ?? ''; };
    setVal('productName', product.name);
    setVal('productNameCn', product.name_cn);
    setVal('productPrice', product.price ?? 0);
    setVal('productCategory', product.category ?? '');
    setVal('productDescription', product.description ?? '');
    setVal('productDescriptionCn', product.description_cn ?? '');

    currentExistingCoverUrl = product.cover_image_url || product.thumbnail_url || '';
    if (currentNewCoverFile?.url) {
        URL.revokeObjectURL(currentNewCoverFile.url);
    }
    currentNewCoverFile = null;

    currentExistingMedia = Array.isArray(product.detail_images) && product.detail_images.length > 0
        ? [...product.detail_images]
        : (Array.isArray(product.media) && product.media.length > 0
        ? [...product.media]
        : []);
    
    if (currentNewFiles.length > 0) {
        currentNewFiles.forEach(item => URL.revokeObjectURL(item.url));
    }
    currentNewFiles = [];
    
    renderCoverPreview();
    renderImagePreviews();

    const submitBtn = document.getElementById('productSubmitBtn');
    if (submitBtn) { submitBtn.innerHTML = '<i class="fas fa-pen"></i> 更新产品'; submitBtn.style.background = '#f39c12'; }
    document.getElementById('cancelEditBtn').style.display = 'inline-block';
}

async function loadProductList() {
    const tbody = document.getElementById('productTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#7f8c8d;padding:20px;">正在加载产品...</td></tr>';

    try {
        const params = new URLSearchParams();
        if (currentProductSearch) {
            params.set('search', currentProductSearch);
        }
        const url = params.toString() ? `${API_ENDPOINTS.LIST_PRODUCTS}?${params.toString()}` : API_ENDPOINTS.LIST_PRODUCTS;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();
        const products = Array.isArray(data) ? data : (data.products || []);
        
        const badge = document.getElementById('productCountBadge');
        if (badge) badge.textContent = String(products.length);
        
        renderProductTable(products);
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e74c3c;padding:20px;">加载失败，请确认后端已启动</td></tr>';
    }
}

function renderProductTable(products) {
    const tbody = document.getElementById('productTableBody');
    if (!tbody) return;

    if (!products.length) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#7f8c8d;padding:20px;">暂无产品数据</td></tr>';
        return;
    }

    tbody.innerHTML = products.map((p) => {
        const id = p.product_id || p.id || '';
        const coverUrl = p.cover_image_url || p.thumbnail_url || '';
        const imageHtml = coverUrl ? `
            <div style="position: relative; width: 52px; height: 52px; display: inline-block;">
                <img src="${coverUrl}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 6px; border: 1px solid #ddd;">
                <span style="position:absolute;left:3px;bottom:3px;background:#27ae60;color:#fff;font-size:9px;padding:1px 5px;border-radius:10px;">封面</span>
            </div>
        ` : '<span style="color: #999; font-size: 12px;">无封面</span>';

        return `
            <tr>
                <td>${id}</td>
                <td style="max-width: 80px;">${imageHtml}</td>
                <td>${escapeHtml(p.name || '')}</td>
                <td>${escapeHtml(p.name_cn || '')}</td>
                <td>${formatPrice(p.price)}</td>
                <td>${escapeHtml(p.category || '')}</td>
                <td>${escapeHtml(p.status || '')}</td>
                <td>
                    <button class="btn-view" data-action="edit" data-id="${id}" style="margin-right:6px;">
                        <i class="fas fa-pen"></i> 编辑
                    </button>
                    <button class="btn-view" data-action="delete" data-id="${id}" style="background:#e74c3c;color:#fff;">
                        <i class="fas fa-trash"></i> 删除
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// ==========================================
// 快捷删除单张图片的逻辑
// ==========================================
async function handleDeleteProductMedia(productId, mediaId, url) {
    if (!window.confirm('确认要直接从产品列表中删除这张图片吗？')) return;

    try {
        if (!mediaId) {
            throw new Error('缺少 media_id，无法删除服务器图片');
        }

        const response = await fetch(`/products/${productId}/images/${mediaId}`, {
            method: 'DELETE'
        });
        const data = await response.json().catch(() => null);
        if (!response.ok) throw new Error(getErrorMessageFromResponse(data, '删除图片失败'));

        showNotification('图片已删除', 'success');
        
        // 如果被删除图片的产品正处于“编辑状态”，清空表单防止再次提交错乱
        if (String(currentEditingProductId) === String(productId)) {
            setFormModeForCreate();
        }
        await loadProductList();

    } catch (error) {
        console.error(error);
        showNotification(error.message || '删除图片失败', 'error');
    }
}

async function handleEditProduct(productId) {
    try {
        const response = await fetch(API_ENDPOINTS.GET_PRODUCT(productId));
        if (!response.ok) throw new Error('获取失败');
        setFormModeForEdit(await response.json());
        const formCard = document.getElementById('productFormCard');
        if (formCard) formCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
        showNotification('加载详情失败', 'error');
    }
}

async function handleDeleteProduct(productId) {
    if (!window.confirm(`确认删除产品 ID: ${productId} ?`)) return;
    try {
        const response = await fetch(API_ENDPOINTS.GET_PRODUCT(productId), { method: 'DELETE' });
        if (!response.ok) throw new Error('删除失败');
        showNotification('产品删除成功', 'success');
        await loadProductList();
        if (String(currentEditingProductId) === String(productId)) setFormModeForCreate();
    } catch (error) {
        showNotification('删除产品失败', 'error');
    }
}

// ==========================================
// 3. 订单管理模块逻辑
// ==========================================
function initOrderManagement() {
    const toggleOrderFormBtn = document.getElementById('toggleOrderFormBtn');
    if (toggleOrderFormBtn) {
        toggleOrderFormBtn.addEventListener('click', () => {
            const formCard = document.getElementById('orderFormCard');
            const isHidden = formCard.style.display === 'none';
            formCard.style.display = isHidden ? 'block' : 'none';
            if (isHidden) resetOrderForm();
        });
    }

    document.getElementById('cancelOrderEditBtn')?.addEventListener('click', () => {
        document.getElementById('orderFormCard').style.display = 'none';
        resetOrderForm();
    });

    const orderForm = document.getElementById('orderForm');
    if (orderForm) orderForm.addEventListener('submit', handleOrderSubmit);

    document.getElementById('refreshOrderListBtn')?.addEventListener('click', loadOrderList);

    const orderTableBody = document.getElementById('orderTableBody');
    if (orderTableBody) {
        orderTableBody.addEventListener('click', async function (e) {
            const btn = e.target.closest('button[data-action]');
            if (!btn) return;
            const action = btn.getAttribute('data-action');
            const id = btn.getAttribute('data-id');
            if (action === 'edit') await handleEditOrder(id);
            if (action === 'delete') await handleDeleteOrder(id);
        });
    }

    loadOrderList();
}

function resetOrderForm() {
    currentEditingOrderId = null;
    document.getElementById('orderForm')?.reset();
    const btn = document.getElementById('orderSubmitBtn');
    if (btn) btn.innerHTML = '<i class="fas fa-save"></i> 保存订单';
}

async function loadOrderList() {
    const tbody = document.getElementById('orderTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">正在加载订单...</td></tr>';

    try {
        const response = await fetch(API_ENDPOINTS.LIST_ORDERS);
        if (!response.ok) throw new Error('失败');
        const data = await response.json();
        const orders = Array.isArray(data) ? data : (data.orders || []);
        const filteredOrders = currentOrderSearch
            ? orders.filter((o) => {
                const keyword = currentOrderSearch.toLowerCase();
                const id = String(o.order_id || o.id || '').toLowerCase();
                const customer = String(o.customer_name || o.customer || '').toLowerCase();
                const status = String(o.status || '').toLowerCase();
                return id.includes(keyword) || customer.includes(keyword) || status.includes(keyword);
            })
            : orders;
        renderOrderTable(filteredOrders);
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#e74c3c;">加载失败，请确认后端已启动</td></tr>';
    }
}

function renderOrderTable(orders) {
    const tbody = document.getElementById('orderTableBody');
    if (!tbody) return;
    if (!orders.length) { tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">暂无订单数据</td></tr>'; return; }

    tbody.innerHTML = orders.map((o) => {
        const id = o.order_id || o.id || '';
        let statusClass = 'status-pending';
        if (o.status === '处理中') statusClass = 'status-processing';
        if (o.status === '已发货') statusClass = 'status-shipped';
        if (o.status === '已交付') statusClass = 'status-delivered';

        return `<tr>
            <td>#${id}</td>
            <td>${escapeHtml(o.customer_name || o.customer || '')}</td>
            <td>$${Number(o.total_amount || o.total || 0).toFixed(2)}</td>
            <td><span class="status-badge ${statusClass}">${escapeHtml(o.status || '待处理')}</span></td>
            <td>${escapeHtml(o.created_at ? new Date(o.created_at).toLocaleDateString() : '-')}</td>
            <td>
                <button class="btn-view" data-action="edit" data-id="${id}" style="margin-right:6px;"><i class="fas fa-pen"></i> 编辑</button>
                <button class="btn-view" data-action="delete" data-id="${id}" style="background:#e74c3c;color:#fff;"><i class="fas fa-trash"></i> 删除</button>
            </td>
        </tr>`;
    }).join('');
}

async function handleOrderSubmit(e) {
    e.preventDefault();
    const payload = {
        customer_name: document.getElementById('orderCustomer').value.trim(),
        total_amount: parseFloat(document.getElementById('orderTotal').value) || 0,
        status: document.getElementById('orderStatus').value
    };

    const isEdit = currentEditingOrderId !== null;
    try {
        const response = await fetch(isEdit ? API_ENDPOINTS.UPDATE_ORDER(currentEditingOrderId) : API_ENDPOINTS.CREATE_ORDER, {
            method: isEdit ? 'PATCH' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error('提交失败');
        showNotification(isEdit ? '订单更新成功' : '订单创建成功', 'success');
        document.getElementById('orderFormCard').style.display = 'none';
        resetOrderForm();
        loadOrderList();
    } catch (error) {
        showNotification('操作失败，请重试', 'error');
    }
}

async function handleEditOrder(orderId) {
    try {
        const response = await fetch(API_ENDPOINTS.GET_ORDER(orderId));
        if (!response.ok) throw new Error('加载失败');
        const order = await response.json();
        
        currentEditingOrderId = orderId;
        document.getElementById('orderFormCard').style.display = 'block';
        document.getElementById('orderCustomer').value = order.customer_name || order.customer || '';
        document.getElementById('orderTotal').value = order.total_amount || order.total || 0;
        document.getElementById('orderStatus').value = order.status || '待处理';
        document.getElementById('orderSubmitBtn').innerHTML = '<i class="fas fa-pen"></i> 更新订单';
        document.getElementById('orderSection').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        showNotification('加载订单详情失败', 'error');
    }
}

async function handleDeleteOrder(orderId) {
    if (!window.confirm(`确认删除订单 #${orderId} ?`)) return;
    try {
        const response = await fetch(API_ENDPOINTS.DELETE_ORDER(orderId), { method: 'DELETE' });
        if (!response.ok) throw new Error('删除失败');
        showNotification('订单删除成功', 'success');
        loadOrderList();
    } catch (error) {
        showNotification('删除订单失败', 'error');
    }
}

// ==========================================
// 全局工具函数
// ==========================================
function getErrorMessageFromResponse(data, fallback) {
    if (!data) return fallback;
    if (typeof data === 'string') return data;
    if (data.error) return data.error;
    if (data.message) return data.message;
    if (data.details && typeof data.details === 'object') {
        const k = Object.keys(data.details)[0];
        if (k) return data.details[k];
    }
    return fallback;
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification-popup ${type}`;
    notification.innerHTML = `<div class="notification-content"><i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i><span>${message}</span></div>`;
    document.body.appendChild(notification);
    setTimeout(() => notification.classList.add('show'), 10);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

(function addNotificationStyles() {
    if (document.getElementById('notification-styles')) return;
    const style = document.createElement('style');
    style.id = 'notification-styles';
    style.textContent = `
        .notification-popup { position: fixed; top: 20px; right: 20px; background: #fff; border-radius: 8px; padding: 15px 20px; box-shadow: 0 4px 12px rgba(0,0,0,.15); z-index: 9999; min-width: 300px; transform: translateX(150%); transition: transform .3s ease; border-left: 4px solid #3498db; }
        .notification-popup.show { transform: translateX(0); }
        .notification-popup.success { border-left-color: #27ae60; }
        .notification-popup.error { border-left-color: #e74c3c; }
        .notification-content { display: flex; align-items: center; gap: 10px; }
    `;
    document.head.appendChild(style);
})();

function escapeHtml(value) {
    return String(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function formatPrice(value) {
    const n = Number(value);
    if (!Number.isFinite(n)) return '¥0.00';
    return `¥${n.toFixed(2)}`;
}
