{- ejercicio 1
a) Bool 
True

b) (Int, Int) 
(5, 7)

c) Char -> Int 
e1c x = 2

d ) (Int, Char) -> Bool 
e1d :: (Integer, Char) -> Bool
e1d (x, y) = 'a' > y || x < 1

e) (Int -> Int) -> Int
e1e :: (Integer -> Integer) -> Integer
e1e f = 1 + (f 1)
f x = x

f ) (Bool -> Bool, Int)
e1f True = (True,1)

g) a -> Bool
e1g x = True

h) c -> c
e1h x = x
-}

--ejercicio 4
first :: (a,b) -> a
first (x,y) = x

second :: (a,b) -> b
second (x,y) = y

const :: a -> b -> a
const x y = x

compose :: (b -> c) -> (a -> b) -> a -> c
compose f g = (\x -> f (g x))

apply :: (a -> b) -> a -> b
apply f x = f x

subst:: (a -> b -> c) -> (a -> b) -> a -> c
subst f g x = f x (g x)

pairFunc :: (a -> d, b -> c) -> a -> b -> (c,d) --corregir
pairFunc (f1,f2) x y = (f1 (f2 x), f2 (f1 y))

{- ejercicio 6
a) Correcto
b) Incorrecto, todo if debe tener else
c) Incorrecto, := es sintacticamente incorrecto
d) Incorrecto, error de tipos
e) Correcta
f) incorrecta, las funciones de mayor, menor, etc son binarias
-}

--ejercicio 8

data ColorPrimario = Azul | Rojo | Verde

--terminar

-- ejercicio 11

smaller :: Int -> Int -> Int -> Int
smaller = \x -> \y -> \z ->
  if (x < y) && (x < z)
    then x
    else if (y < x) && (y < z)
      then y
      else z

{- ejercicio 14
a) (3 == --3) && True
falso, no se pueden poner 2 menos ('-') seguidos

b) 1 && 2 == 2
falso, solo se puede usar el && en booleanos

c) 1 + if (’a’ < ’b’) then 3 else 5
verdadero

-}