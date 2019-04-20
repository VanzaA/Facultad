seven :: Int
seven = 7

sign :: Int -> Int
sign x = if (x > 0)
    then 1 
    else (if (x==0)
        then 0
        else -1)

sign' x | (x > 0) = 1
        | (x == 0) = 0
        | (x < 0) = -1

absolute :: Int -> Int
absolute x = if (x > 0) then x else (x * (-1))

absolute' x | ((sign x) >= 0) = x
            | otherwise = (x * (-1))


and' :: (Bool, Bool) -> Bool
and' (x,y) = if x then y
        else False

prime :: Int -> Bool
prime x = if (x==1) then False else try x (x-1)

try :: Int -> Int -> Bool
try x y = if (y==1) then True else if ((mod x y)==0) then False else try x (y-1)

phi 1 = 2
phi i = test (phi(i-1)) 1

test i j = if (prime (i+j)) then (i+j) else test i (j+1)Ejercicio 3: Refactoring
Indique si cada una de las siguientes aseveraciones es verdadera o falsa. Explique.
1. Cuando un código es refactorizado cambia su comportamiento agregando más
funcionalidad.
2. Si el código está bien refactorizado no es necesario testearlo.
3. Después de ser refactorizado, la estructura interna del código permanece igual que
antes.
4. La refactorización del código se hace en un solo paso en el que se unen todos los
cambios.
