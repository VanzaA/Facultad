-- ejercicio 1

prime :: Int -> Bool
prime x = if (x==1) then False else try x (x-1)

try :: Int -> Int -> Bool
try x y = if (y==1) then True else if ((mod x y)==0) then False else try x (y-1)

phi 1 = 2
phi i = test (phi(i-1)) 1

test i j = if (prime (i+j)) then (i+j) else test i (j+1)