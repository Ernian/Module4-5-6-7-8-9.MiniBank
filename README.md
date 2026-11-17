Запрос:
explain analyze
select a.id as accountId,
t.id as transactionId,
amount,
type,
transaction_date
from accounts a
join transactions t on a.id = t.account_id
where a.id = 2
and (transaction_date between '2025-01-12' and '2026-11-01')
order by transaction_date desc

До добавления индекса вывод:
Sort  (cost=1510.65..1516.67 rows=2410 width=42) (actual time=6.108..6.310 rows=2359 loops=1)
Sort Key: t.transaction_date DESC
Sort Method: quicksort  Memory: 244kB
->  Nested Loop  (cost=0.15..1375.27 rows=2410 width=42) (actual time=0.044..5.332 rows=2359 loops=1)
->  Index Only Scan using accounts_pkey on accounts a  (cost=0.15..8.17 rows=1 width=8) (actual time=0.025..0.029 rows=1 loops=1)
Index Cond: (id = 2)
Heap Fetches: 1
->  Seq Scan on transactions t  (cost=0.00..1343.00 rows=2410 width=42) (actual time=0.017..4.999 rows=2359 loops=1)
Filter: ((transaction_date >= '2025-01-12 00:00:00'::timestamp without time zone) AND (transaction_date <= '2026-11-01 00:00:00'::timestamp without time zone) AND (account_id = 2))
Rows Removed by Filter: 47641
Planning Time: 0.167 ms
Execution Time: 6.456 ms

После добавления индекса вывод:
Sort  (cost=748.84..754.86 rows=2410 width=42) (actual time=1.582..1.741 rows=2359 loops=1)
Sort Key: t.transaction_date DESC
Sort Method: quicksort  Memory: 244kB
->  Nested Loop  (cost=71.17..613.46 rows=2410 width=42) (actual time=0.198..1.039 rows=2359 loops=1)
->  Index Only Scan using accounts_pkey on accounts a  (cost=0.15..8.17 rows=1 width=8) (actual time=0.018..0.020 rows=1 loops=1)
Index Cond: (id = 2)
Heap Fetches: 1
->  Bitmap Heap Scan on transactions t  (cost=71.02..581.19 rows=2410 width=42) (actual time=0.177..0.823 rows=2359 loops=1)
Recheck Cond: ((account_id = 2) AND (transaction_date >= '2025-01-12 00:00:00'::timestamp without time zone) AND (transaction_date <= '2026-11-01 00:00:00'::timestamp without time zone))
Heap Blocks: exact=464
->  Bitmap Index Scan on idx_transactions_account_date  (cost=0.00..70.42 rows=2410 width=0) (actual time=0.130..0.130 rows=2359 loops=1)
Index Cond: ((account_id = 2) AND (transaction_date >= '2025-01-12 00:00:00'::timestamp without time zone) AND (transaction_date <= '2026-11-01 00:00:00'::timestamp without time zone))
Planning Time: 0.117 ms
Execution Time: 1.863 ms

Задание из презентации:

Вывести названия и цены товаров категории 'Электроника' в наличии
SELECT name, price FROM products WHERE category = 'Электроника' AND in_stock = TRUE;

Увеличить цену всех товаров категории 'Книги' на 10%
UPDATE products SET price = (price * 1.10)::bigint WHERE category = 'Книги';

Добавить новый товар: 'Моя первая PostgreSQL книга', 500, 'Книги', true
INSERT INTO products (name, price, category, in_stock, date_of_incoming)
VALUES ('Моя первая PostgreSQL книга', 500, 'Книги', TRUE, CURRENT_DATE);

Удалить все товары, которых нет в наличии
DELETE FROM products WHERE in_stock = FALSE;

Снизить цену книг, доставленных от полугода до 2 лет назад, на 20% и добавить '(Скидочная позиция)' в название
UPDATE products
SET price = (price * 0.80)::bigint,
    name = name || ' (Скидочная позиция)'
WHERE category = 'Книги'
    AND date_of_incoming BETWEEN (CURRENT_DATE - INTERVAL '2 years')
    AND (CURRENT_DATE - INTERVAL '6 months');
