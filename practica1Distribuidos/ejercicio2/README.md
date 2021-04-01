# Establecer un timeout al socket
Para establecer un timeout habra que usar la funcion de abajo.
```
this.socket=new Socket("localhost",port);   //Se conecta el socket a la direccion localhost:8000
this.socket.setSoTimeout(5000);             //Se establece el time out en 5 segundos

```