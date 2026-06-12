const state = {
    token: localStorage.getItem("inventoryOrderToken") || "",
    username: localStorage.getItem("inventoryOrderUsername") || "",
    roles: [],
    isAdmin: false,
    products: [],
    productPage: {
        number: 0,
        size: 10,
        totalPages: 1,
        totalElements: 0
    },
    productQuery: "",
    productStatus: "all",
    inventory: [],
    orders: []
};

const page = document.body.dataset.page || "login";
const $ = (selector) => document.querySelector(selector);

const formatCurrency = (value) => new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
}).format(Number(value || 0));

const formatDate = (value) => value ? new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short"
}).format(new Date(value)) : "";

function showToast(message, isError = false) {
    const toast = $("#toast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.toggle("error", isError);
    toast.hidden = false;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        toast.hidden = true;
    }, 3600);
}

function readErrorMessage(payload) {
    if (!payload) return "";

    try {
        const parsed = JSON.parse(payload);
        return parsed.detail || parsed.message || parsed.error || payload;
    } catch {
        return payload;
    }
}

async function api(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        if (path !== "/api/auth/login") {
            clearSession();
            window.location.href = "/";
        }
        throw new Error("Phiên đăng nhập không hợp lệ hoặc không đủ quyền.");
    }

    if (!response.ok) {
        const payload = await response.text();
        throw new Error(readErrorMessage(payload) || `Lỗi API ${response.status}`);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function readRoles(token) {
    try {
        const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        const payload = JSON.parse(atob(base64.padEnd(base64.length + (4 - base64.length % 4) % 4, "=")));
        return Array.isArray(payload.roles) ? payload.roles : [];
    } catch {
        return [];
    }
}

function setSession(token, username) {
    state.token = token;
    state.username = username;
    state.roles = readRoles(token);
    state.isAdmin = state.roles.includes("ROLE_ADMIN");
    localStorage.setItem("inventoryOrderToken", token);
    localStorage.setItem("inventoryOrderUsername", username);
}

function clearSession() {
    state.token = "";
    state.username = "";
    state.roles = [];
    state.isAdmin = false;
    localStorage.removeItem("inventoryOrderToken");
    localStorage.removeItem("inventoryOrderUsername");
}

function requireLogin() {
    if (!state.token) {
        window.location.href = "/";
        return false;
    }
    return true;
}

function renderSession() {
    const status = $("#sessionStatus");
    if (status) {
        status.textContent = `Đang đăng nhập: ${state.username || "user"}`;
    }

    document.querySelectorAll("[data-admin-only]").forEach((element) => {
        element.hidden = !state.isAdmin;
        const layout = element.closest(".split-layout");
        if (layout) {
            layout.classList.toggle("single-column", element.hidden);
        }
    });
}

async function loadData() {
    const params = new URLSearchParams({
        page: String(state.productPage.number),
        size: String(page === "products" ? state.productPage.size : 100)
    });
    if (state.productQuery) {
        params.set("q", state.productQuery);
    }

    const [products, inventory, orders] = await Promise.all([
        api(`/api/products?${params}`),
        api("/api/inventory"),
        api("/api/orders")
    ]);

    state.products = Array.isArray(products) ? products : products.content || [];
    state.productPage = Array.isArray(products) ? state.productPage : {
        number: products.number,
        size: products.size,
        totalPages: products.totalPages || 1,
        totalElements: products.totalElements || 0
    };
    state.inventory = inventory || [];
    state.orders = orders || [];
}

function renderDashboard() {
    $("#productCount").textContent = state.productPage.totalElements || state.products.length;
    $("#activeProductCount").textContent = state.products.filter((product) => product.active).length;
    $("#availableTotal").textContent = state.inventory.reduce((sum, item) => sum + item.availableQuantity, 0);
    $("#orderCount").textContent = state.orders.length;

    const recent = [...state.orders]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 6);

    $("#recentOrdersBody").innerHTML = recent.map((order) => `
        <tr>
            <td>#${order.id}</td>
            <td>${escapeHtml(order.customerName)}</td>
            <td><span class="badge ${order.status}">${orderStatusLabel(order.status)}</span></td>
            <td>${formatCurrency(order.totalAmount)}</td>
            <td>${formatDate(order.createdAt)}</td>
        </tr>
    `).join("") || emptyRow(5);
}

function renderProductOptions() {
    const options = state.products
        .filter((product) => product.active)
        .map((product) => `<option value="${product.id}">${escapeHtml(product.sku)} - ${escapeHtml(product.name)}</option>`)
        .join("");

    const stockProductId = $("#stockProductId");
    const orderProductId = $("#orderProductId");
    if (stockProductId) stockProductId.innerHTML = options;
    if (orderProductId) orderProductId.innerHTML = options;
}

function renderProducts() {
    const filteredProducts = state.products.filter((product) => {
        if (state.productStatus === "active") return product.active;
        if (state.productStatus === "inactive") return !product.active;
        return true;
    });

    $("#productsBody").innerHTML = filteredProducts.map((product) => `
        <tr>
            <td>${escapeHtml(product.sku)}</td>
            <td>${escapeHtml(product.name)}</td>
            <td>${formatCurrency(product.price)}</td>
            <td><span class="badge ${product.active ? "ACTIVE" : "INACTIVE"}">${product.active ? "Đang bán" : "Ngừng bán"}</span></td>
            <td>
                ${state.isAdmin ? `<div class="actions">
                    <button class="row-button" type="button" data-edit-product="${product.id}">Sửa</button>
                    <button class="row-button danger" type="button" data-delete-product="${product.id}">Ngừng bán</button>
                </div>` : ""}
            </td>
        </tr>
    `).join("") || emptyRow(5);

    const summary = $("#productsSummary");
    if (summary) {
        summary.textContent = `${state.productPage.totalElements} sản phẩm`;
    }

    const pageInfo = $("#productsPageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Trang ${state.productPage.number + 1}/${Math.max(state.productPage.totalPages, 1)}`;
    }

    const prev = $("#prevProductsPage");
    const next = $("#nextProductsPage");
    if (prev) prev.disabled = state.productPage.number <= 0;
    if (next) next.disabled = state.productPage.number + 1 >= state.productPage.totalPages;
}

function renderInventory() {
    $("#inventoryBody").innerHTML = state.inventory.map((item) => `
        <tr>
            <td>${escapeHtml(item.sku)}</td>
            <td>${escapeHtml(item.name)}</td>
            <td>${item.availableQuantity}</td>
            <td>${item.reservedQuantity}</td>
        </tr>
    `).join("") || emptyRow(4);
}

function renderOrders() {
    const status = $("#orderStatusFilter")?.value || "all";
    const filteredOrders = state.orders.filter((order) => status === "all" || order.status === status);

    $("#ordersBody").innerHTML = filteredOrders.map((order) => `
        <tr>
            <td>#${order.id}</td>
            <td>
                <strong>${escapeHtml(order.customerName)}</strong>
                <span class="muted block">${order.items.map((item) => `${escapeHtml(item.sku)} x${item.quantity}`).join(", ")}</span>
            </td>
            <td><span class="badge ${order.status}">${orderStatusLabel(order.status)}</span></td>
            <td>${formatCurrency(order.totalAmount)}</td>
            <td>
                <div class="actions">
                    <button class="row-button" type="button" data-order-action="confirm" data-order-id="${order.id}">Xác nhận</button>
                    <button class="row-button" type="button" data-order-action="cancel" data-order-id="${order.id}">Hủy</button>
                    <button class="row-button" type="button" data-order-action="return" data-order-id="${order.id}">Trả hàng</button>
                </div>
            </td>
        </tr>
    `).join("") || emptyRow(5);
}

function emptyRow(colspan) {
    return `<tr><td colspan="${colspan}" class="muted">Chưa có dữ liệu</td></tr>`;
}

function resetProductForm() {
    $("#productId").value = "";
    $("#productSku").value = "";
    $("#productName").value = "";
    $("#productDescription").value = "";
    $("#productPrice").value = "";
    $("#productActive").checked = true;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function orderStatusLabel(status) {
    return {
        PENDING: "Chờ xử lý",
        CONFIRMED: "Đã xác nhận",
        CANCELLED: "Đã hủy",
        RETURNED: "Đã trả hàng"
    }[status] || status;
}

function bindCommonActions() {
    const logoutBtn = $("#logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            clearSession();
            window.location.href = "/";
        });
    }

    const refreshBtn = $("#refreshBtn");
    if (refreshBtn) {
        refreshBtn.addEventListener("click", async () => {
            try {
                await loadAndRenderPage();
                showToast("Đã làm mới dữ liệu.");
            } catch (error) {
                showToast(error.message, true);
            }
        });
    }
}

function bindLoginPage() {
    if (state.token) {
        window.location.href = "/dashboard.html";
        return;
    }

    $("#loginForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const username = $("#username").value.trim();
        const password = $("#password").value;

        try {
            const result = await api("/api/auth/login", {
                method: "POST",
                body: JSON.stringify({ username, password })
            });
            setSession(result.token, username);
            window.location.href = "/dashboard.html";
        } catch (error) {
            showToast(error.message, true);
        }
    });

    $("#registerForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const username = $("#registerUsername").value.trim();
        const password = $("#registerPassword").value;

        try {
            const result = await api("/api/auth/register", {
                method: "POST",
                body: JSON.stringify({ username, password })
            });
            setSession(result.token, username);
            window.location.href = "/dashboard.html";
        } catch (error) {
            showToast(error.message, true);
        }
    });
}

function bindProductsPage() {
    $("#productSearch")?.addEventListener("input", debounce(async (event) => {
        state.productQuery = event.target.value.trim();
        state.productPage.number = 0;
        await reloadPageWithToast();
    }, 280));

    $("#productStatusFilter")?.addEventListener("change", (event) => {
        state.productStatus = event.target.value;
        renderProducts();
    });

    $("#prevProductsPage")?.addEventListener("click", async () => {
        if (state.productPage.number <= 0) return;
        state.productPage.number -= 1;
        await reloadPageWithToast();
    });

    $("#nextProductsPage")?.addEventListener("click", async () => {
        if (state.productPage.number + 1 >= state.productPage.totalPages) return;
        state.productPage.number += 1;
        await reloadPageWithToast();
    });

    $("#openProductModalBtn")?.addEventListener("click", () => {
        resetProductForm();
        $("#productModalTitle").textContent = "Thêm sản phẩm";
        $("#productModal").showModal();
    });

    $("#closeProductModalBtn")?.addEventListener("click", () => {
        $("#productModal").close();
    });

    $("#productForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const id = $("#productId").value;
        const payload = {
            sku: $("#productSku").value.trim(),
            name: $("#productName").value.trim(),
            description: $("#productDescription").value.trim(),
            price: $("#productPrice").value,
            active: $("#productActive").checked
        };

        try {
            await api(id ? `/api/products/${id}` : "/api/products", {
                method: id ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            resetProductForm();
            $("#productModal").close();
            await loadAndRenderPage();
            showToast("Đã lưu sản phẩm.");
        } catch (error) {
            showToast(error.message, true);
        }
    });

    $("#resetProductBtn")?.addEventListener("click", resetProductForm);

    $("#productsBody")?.addEventListener("click", async (event) => {
        const editId = event.target.dataset.editProduct;
        const deleteId = event.target.dataset.deleteProduct;

        if (editId) {
            const product = state.products.find((item) => String(item.id) === editId);
            if (!product) return;
            $("#productId").value = product.id;
            $("#productSku").value = product.sku;
            $("#productName").value = product.name;
            $("#productDescription").value = product.description || "";
            $("#productPrice").value = product.price;
            $("#productActive").checked = product.active;
            $("#productModalTitle").textContent = "Cập nhật sản phẩm";
            $("#productModal").showModal();
        }

        if (deleteId) {
            try {
                await api(`/api/products/${deleteId}`, { method: "DELETE" });
                await loadAndRenderPage();
                showToast("Đã ngừng bán sản phẩm.");
            } catch (error) {
                showToast(error.message, true);
            }
        }
    });
}

function bindInventoryPage() {
    $("#stockForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await api("/api/inventory/adjust", {
                method: "POST",
                body: JSON.stringify({
                    productId: Number($("#stockProductId").value),
                    quantity: Number($("#stockQuantity").value)
                })
            });
            await loadAndRenderPage();
            showToast("Đã cập nhật tồn kho.");
        } catch (error) {
            showToast(error.message, true);
        }
    });
}

function bindOrdersPage() {
    $("#orderStatusFilter")?.addEventListener("change", renderOrders);

    $("#orderForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await api("/api/orders", {
                method: "POST",
                body: JSON.stringify({
                    customerName: $("#customerName").value.trim(),
                    items: [{
                        productId: Number($("#orderProductId").value),
                        quantity: Number($("#orderQuantity").value)
                    }]
                })
            });
            $("#orderForm").reset();
            $("#orderQuantity").value = 1;
            await loadAndRenderPage();
            showToast("Đã tạo đơn hàng.");
        } catch (error) {
            showToast(error.message, true);
        }
    });

    $("#ordersBody")?.addEventListener("click", async (event) => {
        const orderId = event.target.dataset.orderId;
        const action = event.target.dataset.orderAction;
        if (!orderId || !action) return;

        try {
            await api(`/api/orders/${orderId}/${action}`, { method: "POST" });
            await loadAndRenderPage();
            showToast("Đã cập nhật đơn hàng.");
        } catch (error) {
            showToast(error.message, true);
        }
    });
}

async function loadAndRenderPage() {
    setLoadingState();
    await loadData();
    renderSession();
    renderProductOptions();

    if (page === "dashboard") renderDashboard();
    if (page === "products") renderProducts();
    if (page === "inventory") renderInventory();
    if (page === "orders") renderOrders();
}

async function reloadPageWithToast() {
    try {
        await loadAndRenderPage();
    } catch (error) {
        showToast(error.message, true);
    }
}

function setLoadingState() {
    const bodyByPage = {
        dashboard: $("#recentOrdersBody"),
        products: $("#productsBody"),
        inventory: $("#inventoryBody"),
        orders: $("#ordersBody")
    };
    const body = bodyByPage[page];
    if (body) {
        body.innerHTML = `<tr class="loading-row"><td colspan="5">Đang tải dữ liệu...</td></tr>`;
    }
}

function debounce(callback, delay) {
    let timer;
    return (...args) => {
        window.clearTimeout(timer);
        timer = window.setTimeout(() => callback(...args), delay);
    };
}

async function init() {
    state.roles = readRoles(state.token);
    state.isAdmin = state.roles.includes("ROLE_ADMIN");

    if (page === "login") {
        bindLoginPage();
        return;
    }

    if (!requireLogin()) return;

    renderSession();
    bindCommonActions();
    if (page === "products") bindProductsPage();
    if (page === "inventory") bindInventoryPage();
    if (page === "orders") bindOrdersPage();

    try {
        await loadAndRenderPage();
    } catch (error) {
        showToast(error.message, true);
    }
}

init();
