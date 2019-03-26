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
        else false
