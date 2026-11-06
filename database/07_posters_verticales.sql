-- =====================================================================
--  NovaPop — Script 07: pósters verticales
--  Agrega contenido.poster_vertical_url (formato 2:3, como la carátula de
--  un DVD) y la llena con los pósters de TMDB. poster_url sigue guardando la
--  imagen horizontal (16:9), que se usa en el carrusel principal y en
--  "Seguir viendo".
--  Ejecutar DESPUÉS de 01 a 06. Se puede volver a ejecutar sin problemas.
--
--  Imágenes: TMDB (https://www.themoviedb.org).
--  "This product uses the TMDB API but is not endorsed or certified by TMDB."
-- =====================================================================
SET search_path TO novapop;

ALTER TABLE contenido ADD COLUMN IF NOT EXISTS poster_vertical_url VARCHAR(500);
COMMENT ON COLUMN contenido.poster_vertical_url IS 'Póster vertical 2:3 (tarjetas del catálogo); poster_url es la imagen horizontal 16:9';

-- Solo actualiza los títulos que existan (los de 04 pueden no estar cargados)
UPDATE contenido c
   SET poster_vertical_url = v.url
  FROM (VALUES
    (1, 'https://image.tmdb.org/t/p/w500/i9jJzvoXET4D9pOkoEwncSdNNER.jpg'),
    (2, 'https://image.tmdb.org/t/p/w500/9zROtU9TkpZQrOuEaMAp68FOWLK.jpg'),
    (3, 'https://image.tmdb.org/t/p/w500/l98Zns4oQe1BSvbF4hJ8oL6VSf0.jpg'),
    (4, 'https://image.tmdb.org/t/p/w500/8qy3jRmaHR7f8VZh3iXCqCWfFsH.jpg'),
    (5, 'https://image.tmdb.org/t/p/w500/5ZXi0oitpEgAdoJglFTc5SZF9nt.jpg'),
    (6, 'https://image.tmdb.org/t/p/w500/g5dSJtpoXYCSVx70srSAc3wqNFc.jpg'),
    (7, 'https://image.tmdb.org/t/p/w500/ynuH5sjCLHhlwN3bKW8yWxrMbbl.jpg'),
    (8, 'https://image.tmdb.org/t/p/w500/nei66j5yYOsXzX71BUSnzv9kFlZ.jpg'),
    (9, 'https://image.tmdb.org/t/p/w500/tFaE5KZOzTTTaNAEDD8CY1ZBjei.jpg'),
    (10, 'https://image.tmdb.org/t/p/w500/66VPke0YSiyfe97aobbcZ55ts56.jpg'),
    (500, 'https://image.tmdb.org/t/p/w500/z45rXmIF0zl4uxRc5XcSfz6hYO6.jpg'),
    (501, 'https://image.tmdb.org/t/p/w500/l58KDN46lKx5RPzu1encomzSuJU.jpg'),
    (502, 'https://image.tmdb.org/t/p/w500/wRw5Ur4FJYJHqie3EI6PWKORF99.jpg'),
    (503, 'https://image.tmdb.org/t/p/w500/kVRGAgqUC2u75pLsXgDj14tOsRJ.jpg'),
    (504, 'https://image.tmdb.org/t/p/w500/udWCrPAYCzZh6rp48dGr90ohJ2v.jpg'),
    (505, 'https://image.tmdb.org/t/p/w500/hrAEYBMb6vitzXMvBC7RKj3ZyWt.jpg'),
    (506, 'https://image.tmdb.org/t/p/w500/hrtW78FNfwFSiIetgey0cR8AT0g.jpg'),
    (1000, 'https://image.tmdb.org/t/p/w500/bFj7XRg5avQDvuvWaag3IttjEAw.jpg'),
    (1001, 'https://image.tmdb.org/t/p/w500/p4NYZXVtgKf6aiH65QzzkUVJcsd.jpg'),
    (1002, 'https://image.tmdb.org/t/p/w500/ayyJVOV5I4MGjti7nIHC3mVCagR.jpg'),
    (1003, 'https://image.tmdb.org/t/p/w500/qFAVW4XJaxhj7PcpiUI5hhO9bOX.jpg'),
    (1004, 'https://image.tmdb.org/t/p/w500/82ucHZ4ioVGiweT1XMl1mUZaodq.jpg'),
    (1005, 'https://image.tmdb.org/t/p/w500/cWNIua1iPA4kGyQxJGvnx7UIzoT.jpg'),
    (1006, 'https://image.tmdb.org/t/p/w500/ky38H2S7bWVl46OtAH5kJv6pYAw.jpg'),
    (1007, 'https://image.tmdb.org/t/p/w500/cs5YLwGBCJBold74avD3NecO5Zc.jpg'),
    (1008, 'https://image.tmdb.org/t/p/w500/3Zv12uAqBxj4xAdeHeVySEQzY1o.jpg'),
    (1009, 'https://image.tmdb.org/t/p/w500/zqZNnuMtrmK99Xux91KiCdnZxgb.jpg'),
    (1010, 'https://image.tmdb.org/t/p/w500/ggyEm4OkePLo8zEdDm5Pac2fOgz.jpg'),
    (1011, 'https://image.tmdb.org/t/p/w500/tMJ0FBFrYZjo6BNHiYbgqiy8Tf5.jpg'),
    (1012, 'https://image.tmdb.org/t/p/w500/jPPy7tCfglppQo6J9nGwU6UmJ8X.jpg'),
    (1013, 'https://image.tmdb.org/t/p/w500/sOsvKTJS0XwtfLsNMO3C0CVWJ4u.jpg'),
    (1014, 'https://image.tmdb.org/t/p/w500/kdg6Y06jfq9FV7qknWNcKLYtBJn.jpg'),
    (1015, 'https://image.tmdb.org/t/p/w500/h1Iq6WfE4RWc9klGvN8sdi5aR6V.jpg'),
    (1016, 'https://image.tmdb.org/t/p/w500/rGaayCxGkHZLUyDDB3QQnDg2MHQ.jpg'),
    (1017, 'https://image.tmdb.org/t/p/w500/lAPzezdc5E6DFnttJwCWXZ9A9C6.jpg'),
    (1018, 'https://image.tmdb.org/t/p/w500/G7GOAKUjxIjMUVKBhXOviohEB0.jpg'),
    (1019, 'https://image.tmdb.org/t/p/w500/bvYI6i9lQ3bsup9PgnMF3YYr8ZR.jpg'),
    (1020, 'https://image.tmdb.org/t/p/w500/5SPa7dZ85p54xa7E9tHRmfKq5ce.jpg'),
    (1021, 'https://image.tmdb.org/t/p/w500/uIPTS25whXkAAfRqptuPX4y4XiS.jpg'),
    (1022, 'https://image.tmdb.org/t/p/w500/x4Ldcddhk8jzn3nqeODA8lHNw9M.jpg'),
    (1023, 'https://image.tmdb.org/t/p/w500/9OGkKMnYgSc0TzSIErcOOiKllrK.jpg'),
    (1024, 'https://image.tmdb.org/t/p/w500/6aY3OzCIdxoBMYdiH5s17rWFFFA.jpg'),
    (1025, 'https://image.tmdb.org/t/p/w500/jDpFqbbB2tQ1dPmPf1sV4sWqwvm.jpg'),
    (1026, 'https://image.tmdb.org/t/p/w500/sAluF7lNc4Mv3qxx1mmOgsfbr0C.jpg'),
    (1027, 'https://image.tmdb.org/t/p/w500/xXBX2G5dc2WUZslcWuAXYKvYKoI.jpg'),
    (1028, 'https://image.tmdb.org/t/p/w500/90Shq6yU7vqXskmZbX2AEf57ddy.jpg'),
    (1029, 'https://image.tmdb.org/t/p/w500/g4UFXF8JP4W5nBP3ih26mLv50hX.jpg'),
    (1030, 'https://image.tmdb.org/t/p/w500/zOJKtTdToc8FcjmmbavkjUxv2Ue.jpg'),
    (1031, 'https://image.tmdb.org/t/p/w500/tDvGRWSdqT31ADijJf9OhbTbQ77.jpg'),
    (1032, 'https://image.tmdb.org/t/p/w500/vHbizQYlkL8MwtOdsdWaPuR5N5w.jpg'),
    (1033, 'https://image.tmdb.org/t/p/w500/ofoVXR8gBYhV4e0MpRQtSpeXLiK.jpg'),
    (1034, 'https://image.tmdb.org/t/p/w500/vH7avbxSgyTMVvwaevhL08yDOY.jpg'),
    (1035, 'https://image.tmdb.org/t/p/w500/uBKffnwwHGIkQET18gVPsyeDdjC.jpg'),
    (1036, 'https://image.tmdb.org/t/p/w500/Vc0KvO7z2OzEbRs6nyZs9xD81s.jpg'),
    (1037, 'https://image.tmdb.org/t/p/w500/1FxMtEUc6DP1MXsTBftOFaoCVVO.jpg'),
    (1038, 'https://image.tmdb.org/t/p/w500/qiEpw4qjwYw82Qzme56UZwWJOAG.jpg'),
    (1039, 'https://image.tmdb.org/t/p/w500/b0MxU37dNmMwKtoPVYPKOZSIrIn.jpg'),
    (1040, 'https://image.tmdb.org/t/p/w500/o8QGvm0zDrJ8aONM8G5dW4BNawG.jpg'),
    (1041, 'https://image.tmdb.org/t/p/w500/l01E2WNKOir8RRS0atzjkxt49oN.jpg'),
    (1042, 'https://image.tmdb.org/t/p/w500/4Q2rLG6a9fJhKM5emIYLzgw4HCa.jpg'),
    (1043, 'https://image.tmdb.org/t/p/w500/fKAoEAyCaIxVE4TGlXPzkz88Rnn.jpg'),
    (1044, 'https://image.tmdb.org/t/p/w500/1Gk8iihu4Q4BGh2n1IwNLB3zM8E.jpg'),
    (1045, 'https://image.tmdb.org/t/p/w500/wZdRUZX624bTFo2drYrrBjoNL7Y.jpg'),
    (1046, 'https://image.tmdb.org/t/p/w500/o2muinN4yIeU4OR9DcEi0tXdqO7.jpg'),
    (1047, 'https://image.tmdb.org/t/p/w500/qYckc7dwtuIUlFEx1lallXFfYka.jpg'),
    (1048, 'https://image.tmdb.org/t/p/w500/hGB7JQonWl7OquqTFdQtKCk7hGk.jpg'),
    (1049, 'https://image.tmdb.org/t/p/w500/sDK83BJirIyEpHwtm4e3CpkS4q2.jpg'),
    (1050, 'https://image.tmdb.org/t/p/w500/n8V3EJxD78JZvKputjBCAgPjB3x.jpg'),
    (1051, 'https://image.tmdb.org/t/p/w500/yplFdLSePwRdVeiw3xfnumeTvsH.jpg'),
    (1052, 'https://image.tmdb.org/t/p/w500/vTpZl5n7wxyxhZZINd3gDVaEGhk.jpg'),
    (1053, 'https://image.tmdb.org/t/p/w500/jOIH3wCtz3y4DonPDoHqSz0VjDj.jpg'),
    (1054, 'https://image.tmdb.org/t/p/w500/6eCyJHdj6XiW0PVyj7WWPlgmQ9H.jpg'),
    (1055, 'https://image.tmdb.org/t/p/w500/ouRndgsflwbSs54yWIQYwPDqIzl.jpg'),
    (1056, 'https://image.tmdb.org/t/p/w500/mcZ8PPHgv13uo9U3BFeyUzXGD4B.jpg'),
    (1057, 'https://image.tmdb.org/t/p/w500/hVa3OCfwqFm6K3Sk6IF68cYr7uk.jpg'),
    (1058, 'https://image.tmdb.org/t/p/w500/cxVlgYbn1GAdVhuBjM2jFiavfRp.jpg'),
    (1059, 'https://image.tmdb.org/t/p/w500/xyf7movibXeZEMaPo0NcNf1G2YE.jpg'),
    (1060, 'https://image.tmdb.org/t/p/w500/wKHHsu1awdIUHq6OlkMoxEPNylB.jpg'),
    (1061, 'https://image.tmdb.org/t/p/w500/kakv4ftQ5TlfjtlXwI3DcsbiTYa.jpg'),
    (1062, 'https://image.tmdb.org/t/p/w500/1BIovowG61saUJuxdzZdG963uw6.jpg'),
    (1063, 'https://image.tmdb.org/t/p/w500/h6zm5DOu6Fm146nU0iqYkhkG6DO.jpg'),
    (1064, 'https://image.tmdb.org/t/p/w500/5PWLsq6g52HqBMVJPVj64Q77p1L.jpg'),
    (1065, 'https://image.tmdb.org/t/p/w500/3iD8i2t8UkgeL8TOJOy6hQvUKmW.jpg'),
    (1066, 'https://image.tmdb.org/t/p/w500/qGu6CHagpxv1XW6Bn3sUJ9SFaH0.jpg'),
    (1067, 'https://image.tmdb.org/t/p/w500/5SbIDk0Gkyr0y22ZtydVq9Px423.jpg'),
    (1068, 'https://image.tmdb.org/t/p/w500/vvxsrGizhFELRqhWqiR8uSUDwh0.jpg'),
    (1069, 'https://image.tmdb.org/t/p/w500/anFx9aTOOYqgS3v7x3R84Kz67ly.jpg'),
    (1070, 'https://image.tmdb.org/t/p/w500/nvqQ6K2JiqpSegSaWVkYt5wD610.jpg'),
    (1071, 'https://image.tmdb.org/t/p/w500/sADRlqLn9na8p7S1mLWMOU04uit.jpg'),
    (1072, 'https://image.tmdb.org/t/p/w500/3A4IzhQHCzX5nuQParZp07F5rCT.jpg')
  ) AS v(id, url)
 WHERE c.id_contenido = v.id;
