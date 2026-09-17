import React, { useState, useEffect } from 'react';

const API_BASE = 'http://localhost:8080/api';

export default function App() {
  const [inventory, setInventory] = useState([]);
  const [orders, setOrders] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [cart, setCart] = useState([]);
  const [message, setMessage] = useState('');

  const refreshData = async () => {
    try {
      const [invRes, ordRes, notifRes] = await Promise.all([
        fetch(`${API_BASE}/inventory`),
        fetch(`${API_BASE}/orders`),
        fetch(`${API_BASE}/notifications`)
      ]);

      if (invRes.ok) setInventory(await invRes.ok ? await invRes.json() : []);
      if (ordRes.ok) setOrders(await ordRes.json());
      if (notifRes.ok) setNotifications(await notifRes.json());
    } catch (err) {
      console.error('Error connecting to backend:', err);
    }
  };

  useEffect(() => {
    refreshData();
  }, []);

  const addToCart = (productId) => {
    setCart((prevCart) => {
      const existing = prevCart.find((item) => item.productId === productId);
      if (existing) {
        return prevCart.map((item) =>
          item.productId === productId
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prevCart, { productId, quantity: 1 }];
    });
  };

  const updateCartQuantity = (productId, quantity) => {
    if (quantity <= 0) {
      setCart(cart.filter((item) => item.productId !== productId));
    } else {
      setCart(
        cart.map((item) =>
          item.productId === productId ? { ...item, quantity: parseInt(quantity, 10) } : item
        )
      );
    }
  };

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    if (cart.length === 0) return;

    try {
      const response = await fetch(`${API_BASE}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ items: cart })
      });

      const data = await response.json();
      setMessage(`Order Status: ${data.status} - ${data.reason}`);
      setCart([]);
      refreshData();
    } catch (err) {
      setMessage('Failed to submit order');
    }
  };

  const handleCancelOrder = async (orderId) => {
    try {
      const response = await fetch(`${API_BASE}/orders/${orderId}/cancel`, {
        method: 'POST'
      });

      if (response.ok) {
        setMessage(`Order #${orderId} cancelled successfully.`);
      } else {
        const errorText = await response.text();
        setMessage(`Cancel Failed: ${errorText}`);
      }
      refreshData();
    } catch (err) {
      setMessage('Failed to cancel order');
    }
  };

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>Shop Admin & Order Dashboard</h1>

      {message && (
        <div style={{ padding: '1rem', background: '#e2e8f0', marginBottom: '1rem', borderRadius: '4px' }}>
          <strong>Notification:</strong> {message}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        <div>
          <h2>Available Inventory</h2>
          <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th>Product ID</th>
                <th>Name</th>
                <th>Stock</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {inventory.map((item) => {
                const isLowStock = item.stock < 5;
                return (
                  <tr
                    key={item.productId}
                    style={{
                      backgroundColor: isLowStock ? '#fee2e2' : 'transparent',
                      color: isLowStock ? '#991b1b' : 'inherit'
                    }}
                  >
                    <td>{item.productId}</td>
                    <td>{item.name}</td>
                    <td>
                      {item.stock} {isLowStock && <strong>(Low Stock!)</strong>}
                    </td>
                    <td>
                      <button onClick={() => addToCart(item.productId)}>Add to Cart</button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          <h2 style={{ marginTop: '2rem' }}>Multi-Item Cart</h2>
          {cart.length === 0 ? (
            <p>Cart is empty.</p>
          ) : (
            <form onSubmit={handlePlaceOrder}>
              <ul style={{ paddingLeft: '1.2rem' }}>
                {cart.map((item) => (
                  <li key={item.productId} style={{ marginBottom: '0.5rem' }}>
                    <strong>{item.productId}</strong> - Qty:{' '}
                    <input
                      type="number"
                      min="1"
                      value={item.quantity}
                      onChange={(e) => updateCartQuantity(item.productId, e.target.value)}
                      style={{ width: '60px', marginLeft: '0.5rem' }}
                    />
                  </li>
                ))}
              </ul>
              <button type="submit" style={{ padding: '0.5rem 1rem', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                Submit Order
              </button>
            </form>
          )}
        </div>

        <div>
          <h2>Live Activity Feed</h2>
          <div style={{ height: '200px', overflowY: 'auto', border: '1px solid #ccc', padding: '1rem', background: '#f8fafc' }}>
            {notifications.length === 0 ? (
              <p>No activity recorded yet.</p>
            ) : (
              <ul style={{ paddingLeft: '1rem', margin: 0 }}>
                {notifications.slice().reverse().map((n) => (
                  <li key={n.notificationId} style={{ marginBottom: '0.5rem' }}>
                    <small>{new Date(n.createdAt).toLocaleTimeString()}</small>: {n.message}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <h2 style={{ marginTop: '2rem' }}>Order History</h2>
          <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th>Order ID</th>
                <th>Items</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((ord) => (
                <tr key={ord.orderId}>
                  <td>#{ord.orderId}</td>
                  <td>
                    {ord.items ? (
                      ord.items.map((i) => `${i.productId} (x${i.quantity})`).join(', ')
                    ) : (
                      'N/A'
                    )}
                  </td>
                  <td>
                    <strong>{ord.status}</strong>
                  </td>
                  <td>
                    {ord.status === 'CONFIRMED' && (
                      <button
                        onClick={() => handleCancelOrder(ord.orderId)}
                        style={{ background: '#dc2626', color: '#fff', border: 'none', padding: '0.25rem 0.5rem', borderRadius: '3px', cursor: 'pointer' }}
                      >
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}