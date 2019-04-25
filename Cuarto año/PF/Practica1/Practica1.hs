-- ejercicio 1

seven :: a -> Int
seven x = 7

sign :: Int -> Int
sign 0 = 0
sign x = if x > 0 then 1 else -1

sign' x | x > 0 = 1
        | x < 0 = -1
        | x == 0 = 0

absolute x = if sign x >= 0 then x else x * -1

and' :: Bool -> Bool -> Bool
or' :: Bool -> Bool -> Bool
xor' :: Bool -> Bool -> Bool
not' ::  Bool -> Bool

and' a b = if a == True then b else a

or' a b = if a == True then a else b

not' a = if a == True then False else True

xor' a b = if b == True then not' a else a

dividesTo :: Int -> Int -> Bool
dividesTo a b = if b `mod` a > 0 then False else True

isMultiple :: Int -> Int -> Bool
isMultiple a b = if a `mod` b > 0 then False else True

isCommonDivisor :: Int -> (Int, Int) -> Bool
isCommonDivisor a (b,c) = if dividesTo a b then dividesTo a c else False

swap :: (Int, Int) -> (Int, Int)
swap (x,y) = (y,x)

-- ejercicio 2

--f x = let (y,z) = (x,x) in y
f x = x

--greaterThan (x,y) = if x > y then True else False
greaterThan (x,y) = x > y

--f (x,y) = let z = x + y in g (z,y) where g (a,b) = a - b
f' (x,y) = x

-- ejercicio 3

power4''' x = let sqr y = y * y
           in sqr (sqr x)

power4' x = x * x * x * x

power4'' x = sqr (sqr x) where sqr y = y * y

-- ejercicio 4

t 0 = 1 
t 1 = 1
t n = t (n - 1) + t (n - 2)

-- ejercicio 9

esBisiesto x = (mod (mod x 100) 4 == 0 && mod x 100 /= 0) || mod x 400 == 0
