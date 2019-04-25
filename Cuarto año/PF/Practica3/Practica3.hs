--ejercicio 2

bul :: Bool -> Bool -> Bool
bul x y = x && y 

b)
first :: a -> b -> a
firstOne x y = x

c)
fc :: (a -> b) -> (a -> c) -> a -> (b, c)
fc x y z = (x z, y z)

d)
fd :: Int -> Int -> Int
fd x y = x + y

e)
fe :: (Int -> Int) -> Int -> Int
fe f x = f x + x -- total

f)
ff :: (a -> b -> c) -> b -> a -> c
ff x y z = y x z
