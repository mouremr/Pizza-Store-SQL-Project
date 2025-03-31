--Indexes for Users table
CREATE INDEX index_users_phoneNum ON Users(phoneNum);

--Indexes for Items table
CREATE INDEX index_items_type ON Items(typeOfItem);
CREATE INDEX index_items_price ON Items(price);

--Indexes for Store table
CREATE INDEX index_store_city ON Store(city);
CREATE INDEX index_store_state ON Store(state);
CREATE INDEX index_store_reviewScore ON Store(reviewScore);

--Indexes for FoodOrder table
CREATE INDEX index_foodorder_login ON FoodOrder(login);
CREATE INDEX index_foodorder_storeID ON FoodOrder(storeID);
CREATE INDEX index_foodorder_orderStatus ON FoodOrder(orderStatus);
CREATE INDEX index_foodorder_orderTimestamp ON FoodOrder(orderTimestamp);

--Indexes for ItemsInOrder table
CREATE INDEX index_itemsinorder_itemName ON ItemsInOrder(itemName);
CREATE INDEX index_itemsinorder_orderID ON ItemsInOrder(orderID);