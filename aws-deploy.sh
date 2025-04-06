#!/bin/bash

BASE_URL="http://localhost:8082/api"
SHOP_ID="123e4567-e89b-12d3-a456-426614174000"
MENU_ITEM_1="123e4567-e89b-12d3-a456-426614174001"
MENU_ITEM_2="123e4567-e89b-12d3-a456-426614174002"

# Place an Order
echo "📦 Placing order..."
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d "{
    \"shopId\": \"$SHOP_ID\",
    \"items\": [
      { \"menuItemId\": \"$MENU_ITEM_1\", \"quantity\": 2 },
      { \"menuItemId\": \"$MENU_ITEM_2\", \"quantity\": 1 }
    ]
  }")

ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"orderId":[0-9]*' | cut -d':' -f2)

echo "✅ Order placed with ID: $ORDER_ID"
echo "Raw response:"
echo "$ORDER_RESPONSE"
echo ""

# Get Order Status
echo "🔎 Fetching order status..."
curl -s "$BASE_URL/orders/$ORDER_ID"
echo ""

# Serve the order
echo "☕ Serving order $ORDER_ID"
curl -s -X PATCH "$BASE_URL/orders/$ORDER_ID/serve"
echo ""
echo "✅ Order served."
echo ""

# Cancel the order (should fail if already served)
echo "❌ Trying to cancel order $ORDER_ID"
curl -s -X PATCH "$BASE_URL/orders/$ORDER_ID/cancel"
echo ""

echo "--------------------------------"
# View Queue Snapshot
echo "📋 Queue snapshot for shop $SHOP_ID"
curl -s "$BASE_URL/shops/$SHOP_ID/queue"
echo ""
