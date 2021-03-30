# Media de la latencia en ficheros de texto
Para el tiempo que se espera el proxy a una respuesta por parte de los servidores se usara la media de las ultimas latencias, las cuales se guardaran en un fichero de texto 
unico para cada servidor.
```
 long end =System.currentTimeMillis(); //Se finaliza el contador que antes se habia iniciado
            System.out.println("Latency server "+ this.indexServer+": " +(end-start)+" ms"); //Se muestra por pantalla el tiempo que ha tardado cada uno de los servidores
            File file= new File("mean"+this.indexServer+".txt"); //Se inicializa el fichero de texto
            if(file.exists()) { // Si el fichero existe se ejecuta esta logica
            	PrintWriter outputFile;
              try {
                String latency=""; //Cadena de texto donde se guardan las anteriores latencias
                Scanner scanner = new Scanner(file);             //inicializamos el scanner 
                while(scanner.hasNext()) {                       //Mientras haya mas caracteres se ejecutara este bloque
                             latency +=scanner.next();           //Se añade la latencia
                             latency +=" ";                      //Se añade un espacio
                          }
                          scanner.close(); //Cerramos el escaner 
                outputFile = new PrintWriter(file);              //inicializamos la variable encargarda de escribir en el fichero 
                String text=latency+(end-start);                 //creamos el texto que vamos a escribir

                outputFile.print(text);                          //se escribe en el fichero
                        outputFile.close();                      //se cierra el writter
              } catch (FileNotFoundException e) {

                System.out.println(e.getMessage());              //En caso de fallo se muestra el mismo
              }
            	

            }else {
            	System.out.println("The file "+file.getName()+" does not exists"); //Si la carpeta no existe se informa al usuario
            }
```
Para saber la media de dichos tiempos usamos el siguiente bloque de codigo

```
  File file= new File("mean"+this.indexServer+".txt"); //Se inicializa el fichero de texto
        	long sleep= 300; //Se establece un tiempo por defecto de 300 milisegundos
            if(file.exists()) {
            	
                try {
                  int i = 0;                               //Se guarda el numero de latencias
                  int sum = 0;                             //Se guarda la suma de estas latencias
                  Scanner scanner = new Scanner(file);     //Se inicializa el scanner
                  while(scanner.hasNext()) {               //Mientras haya mas numeros en el fichero se ejecutara este bloque
                               sum +=scanner.nextInt();    //Se suman todas las variables del interior del fichero
                               i+=1;                       //Se sumara uno por cada latencia
                            }
                            scanner.close();               //se cierra el scanner
                            if(sum!=0) {                   //si ha habido mas de una latencia se hara la media
                              sleep=(sum/i);
                            }
                            System.out.println("Current waiting time: "+sleep+" ms"); //Se muestra por pantalla el tiempo que se esperara


                } catch (FileNotFoundException e) {

                  System.out.println(e.getMessage());       //En caso de fallo se muestra el mismo
                }
            	

            }else {
            	System.out.println("The file "+file.getName()+" does not exists"); //Si la carpeta no existe se informa al usuario
            }

```

# Movilidad en caso de fallo en el proxy principal

En caso de que el proxy principal falle, otro proxy tomara el cargo, siguiendo por el mismo punto donde lo dejo el otro.
Los cambios se encuentran en el cliente, ya que añadir un proxy es solamente multiplicar el codigo cambiando solamente el puerto donde escucha

```
private void doConnect(int port) {
        try{
            if(this.socket == null) {                                                  //Si el socket es null, significa que no hay conexion establecida
                this.socket = new Socket("localhost", port);                           //Se inicializa el socket
                this.os = new ObjectOutputStream(this.socket.getOutputStream());       //Se inicializa el canal de salida
                this.is = new ObjectInputStream(this.socket.getInputStream());         //Se inicializa el canal de entrada
            }
        }catch(UncheckedIOException e) {                                               //En caso de que haya algun fallo se ejecutara el bloque del catch
            System.out.println(e.getMessage()+ "\n Establishing connection with proxy 2 ...");
            int port2 = port+1;                                                 //Se suma uno al puerto donde se iba a conectar debido que el segundo proxy escuchara ese puerto
            this.doConnect(port2);                                              //Se conectara a dicho puerto ejecutando de nuevo el bloque try
        }catch(IOException e) {
            System.out.println(e.getMessage()+"\n Establishing connection with proxy 2 ...");
            int port2 = port+1;
            this.doConnect(port2);
        }
    }
```
