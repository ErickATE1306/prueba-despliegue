const API_URL = '/api/productos';
const form = document.querySelector('#producto-form');
const buscar = document.querySelector('#buscar');
const tabla = document.querySelector('#productos-body');
const emptyState = document.querySelector('#empty-state');
const guardarBtn = document.querySelector('#guardar-btn');
const cancelarBtn = document.querySelector('#cancelar-btn');
const toast = document.querySelector('#toast');
const moneda = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });

let productos = [];
let productoEditando = null;
let toastTimeout;

async function solicitar(url, opciones = {}) {
    const respuesta = await fetch(url, {
        ...opciones,
        headers: { 'Content-Type': 'application/json', ...opciones.headers }
    });
    if (respuesta.status === 204) return null;

    const contenido = await respuesta.json();
    if (!respuesta.ok) {
        const detalle = contenido.errores && Object.values(contenido.errores)[0];
        throw new Error(detalle || contenido.mensaje || 'No se pudo completar la operación');
    }
    return contenido;
}

function avisar(mensaje, error = false) {
    clearTimeout(toastTimeout);
    toast.textContent = mensaje;
    toast.classList.toggle('error', error);
    toast.classList.remove('hidden');
    toastTimeout = setTimeout(() => toast.classList.add('hidden'), 4000);
}

async function cargarProductos() {
    try {
        productos = await solicitar(API_URL);
        renderizar();
    } catch (error) {
        avisar(error.message, true);
    }
}

function celda(texto, clase = '') {
    const td = document.createElement('td');
    if (clase) td.className = clase;
    td.textContent = texto;
    return td;
}

function renderizar() {
    const termino = buscar.value.trim().toLocaleLowerCase('es');
    const visibles = productos.filter(producto =>
        [producto.codigo, producto.nombre, producto.categoria]
            .some(valor => valor.toLocaleLowerCase('es').includes(termino))
    );

    document.querySelector('#total-productos').textContent = productos.length;
    document.querySelector('#total-unidades').textContent = productos.reduce((total, producto) => total + producto.stock, 0);
    document.querySelector('#stock-bajo').textContent = productos.filter(producto => producto.stock < 5).length;
    document.querySelector('#result-count').textContent = `${visibles.length} ${visibles.length === 1 ? 'producto' : 'productos'}`;
    emptyState.classList.toggle('hidden', visibles.length !== 0);
    tabla.replaceChildren();

    for (const producto of visibles) {
        const fila = document.createElement('tr');
        const identificacion = document.createElement('td');
        const nombre = document.createElement('span');
        nombre.className = 'product-name';
        nombre.textContent = producto.nombre;
        const codigo = document.createElement('span');
        codigo.className = 'product-code';
        codigo.textContent = producto.codigo;
        identificacion.append(nombre, codigo);
        fila.append(identificacion);
        fila.append(celda(producto.categoria));
        fila.append(celda(moneda.format(producto.precio), 'price'));

        const stockCelda = document.createElement('td');
        const stock = document.createElement('span');
        stock.className = `stock-pill${producto.stock === 0 ? ' out' : producto.stock < 5 ? ' low' : ''}`;
        stock.textContent = producto.stock === 0 ? 'Agotado' : `${producto.stock} unidades`;
        stockCelda.append(stock);
        fila.append(stockCelda);

        const accionesCelda = document.createElement('td');
        const acciones = document.createElement('div');
        acciones.className = 'row-actions';
        const editar = document.createElement('button');
        editar.type = 'button';
        editar.className = 'icon-button';
        editar.textContent = 'Editar';
        editar.setAttribute('aria-label', `Editar ${producto.nombre}`);
        editar.addEventListener('click', () => editarProducto(producto));
        const eliminar = document.createElement('button');
        eliminar.type = 'button';
        eliminar.className = 'icon-button delete';
        eliminar.textContent = 'Eliminar';
        eliminar.setAttribute('aria-label', `Eliminar ${producto.nombre}`);
        eliminar.addEventListener('click', () => eliminarProducto(producto));
        acciones.append(editar, eliminar);
        accionesCelda.append(acciones);
        fila.append(accionesCelda);
        tabla.append(fila);
    }
}

function limpiarFormulario() {
    productoEditando = null;
    form.reset();
    document.querySelector('#form-title').textContent = 'Nuevo producto';
    guardarBtn.textContent = 'Guardar producto';
    cancelarBtn.classList.add('hidden');
}

function editarProducto(producto) {
    productoEditando = producto.id;
    for (const campo of ['codigo', 'nombre', 'categoria', 'precio', 'stock']) {
        form.elements[campo].value = producto[campo];
    }
    document.querySelector('#form-title').textContent = 'Editar producto';
    guardarBtn.textContent = 'Guardar cambios';
    cancelarBtn.classList.remove('hidden');
    document.querySelector('.form-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
    form.elements.nombre.focus();
}

async function eliminarProducto(producto) {
    if (!confirm(`¿Eliminar "${producto.nombre}" del inventario?`)) return;
    try {
        await solicitar(`${API_URL}/${producto.id}`, { method: 'DELETE' });
        if (productoEditando === producto.id) limpiarFormulario();
        await cargarProductos();
        avisar('Producto eliminado');
    } catch (error) {
        avisar(error.message, true);
    }
}

form.addEventListener('submit', async evento => {
    evento.preventDefault();
    if (!form.reportValidity()) return;

    const datos = {
        codigo: form.elements.codigo.value.trim(),
        nombre: form.elements.nombre.value.trim(),
        categoria: form.elements.categoria.value.trim(),
        precio: Number(form.elements.precio.value),
        stock: Number(form.elements.stock.value)
    };
    const editando = productoEditando !== null;
    guardarBtn.disabled = true;
    try {
        await solicitar(editando ? `${API_URL}/${productoEditando}` : API_URL, {
            method: editando ? 'PUT' : 'POST',
            body: JSON.stringify(datos)
        });
        limpiarFormulario();
        await cargarProductos();
        avisar(editando ? 'Producto actualizado' : 'Producto agregado');
    } catch (error) {
        avisar(error.message, true);
    } finally {
        guardarBtn.disabled = false;
    }
});

cancelarBtn.addEventListener('click', limpiarFormulario);
buscar.addEventListener('input', renderizar);
cargarProductos();
