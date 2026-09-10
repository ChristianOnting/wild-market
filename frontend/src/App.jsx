import { useState } from 'react';

export default function App() {
  const [productId, setProductId] = useState('P100');
  const [quantity, setQuantity] = useState(1);
  const [result, setResult] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setResult(null);
    setErrorMsg('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          productId: productId,
          quantity: parseInt(quantity, 10),
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setResult(data);
      } else {
        setErrorMsg(data.reason || 'Failed to process order');
      }
    } catch (err) {
      setErrorMsg(`Server Error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Place Order</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="product">Product: </label>
          <select
            id="product"
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
          >
            <option value="P100">P100 (In Stock)</option>
            <option value="P200">P200</option>
            <option value="P300">P300 (Out of Stock)</option>
          </select>
        </div>

        <div>
          <label htmlFor="quantity">Quantity: </label>
          <input
            id="quantity"
            type="number"
            min="1"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Submitting...' : 'Submit Order'}
        </button>
      </form>

      {result && (
        <div>
          <p>Status: {result.status}</p>
          <p>Message: {result.reason}</p>
          {result.inventory && (
            <p>
              Updated Stock for {result.inventory.name}: {result.inventory.stock}
            </p>
          )}
        </div>
      )}

      {errorMsg && (
        <div>
          <p>Error: {errorMsg}</p>
        </div>
      )}
    </div>
  );
}