## UDP Chat

Este código implementa un servidor UDP en Java que maneja múltiples clientes de chat. Utiliza `DatagramSocket` para recibir y enviar mensajes a través de paquetes UDP. El servidor también puede almacenar mensajes en un archivo y enviar mensajes previos a los nuevos usuarios que se conecten.


**Servidor UDP (`UdpServer.java`)**:

#### Variables Estáticas

```java
private static byte[] incoming = new byte[256];
private static File f = new File("messages.txt");
private static final int PORT = 8000;
private static DatagramSocket socket;
private static boolean previousMessages = false;
private static HashMap<Integer, String> users = new HashMap<>();
private static final InetAddress address;
```

- `incoming`: Array de bytes para almacenar los datos entrantes.
- `f`: Archivo donde se guardarán los mensajes.
- `PORT`: Puerto en el que el servidor escucha.
- `socket`: Socket UDP del servidor.
- `previousMessages`: Indicador de si hay mensajes previos para mostrar.
- `users`: Mapa para asociar puertos de clientes con nombres de usuario.
- `address`: Dirección IP del servidor.

#### Inicialización Estática

```java
static {
    try {
        socket = new DatagramSocket(PORT);
    } catch (SocketException e) {
        throw new RuntimeException(e);
    }
}

static {
    try {
        address = InetAddress.getByName("localhost");
    } catch (UnknownHostException e) {
        throw new RuntimeException(e);
    }
}
```

Se inicializan el socket y la dirección del servidor. Si hay algún problema, se lanza una excepción en tiempo de ejecución.

#### Método `main`

```java
public static void main(String[] args) {
    System.out.println("Server started on port " + PORT);

    while (true) {
        DatagramPacket packet = new DatagramPacket(incoming, incoming.length); // prepare packet
        try {
            socket.receive(packet); // receive packet
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String message = new String(packet.getData(), 0, packet.getLength()); // create a string
        System.out.println("Server received: " + message);

        int userPort = packet.getPort();  // get port from the packet

        if (message.contains("init; ")) {
            validateUser(message, packet);
            if (previousMessages) showPrevoiusMesssages(socket, packet);
            saveMessage(users.get(userPort), message);
        } else if (message.equals("/quit")) {
            String threadFlag = "exit";
            saveMessage(users.get(userPort), message);
            byte[] threadFlagBytes = threadFlag.getBytes(); // convert the string to bytes

            // This will terminate the ClientThread
            DatagramPacket forward = new DatagramPacket(threadFlagBytes, threadFlagBytes.length, address, userPort);
            try {
                socket.send(forward);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Broadcast to the rest of the users
            String userExited = users.get(packet.getPort()) + " HAS LEFT THE CHAT"; // ANNOUNCEMENT
            broadcastMessage(userExited, userPort);
            users.remove(packet.getPort());
        } else if (message.contains("/nick ")) {
            // rename();
        } else {
            String otherMessage = users.get(userPort) + ": " + message; // This will be shown on the screen whenever someone else types
            broadcastMessage(otherMessage, userPort);
        }
    }
}
```

El método `main()` hace lo siguiente:

1. **Inicio del Servidor**: Muestra un mensaje de inicio.
2. **Bucle Infinito**: El servidor recibe y procesa paquetes continuamente.
3. **Recepción de Paquetes**: Lee paquetes UDP entrantes.
4. **Procesamiento de Mensajes**:
   - **Inicialización de Usuario**: Valida y maneja nuevos usuarios.
   - **Desconexión de Usuario**: Maneja la desconexión de usuarios.
   - **Reenvío de Mensajes**: Reenvía mensajes a todos los usuarios conectados.
   
#### Métodos Auxiliares

- **`broadcastMessage`**: Este método reenvía un mensaje a todos los usuarios conectados excepto al que envió el mensaje original.

```java
private static void broadcastMessage(String message, int userPort){
    byte[] byteMessage = message.getBytes(); // convert the string to bytes
    saveMessage(users.get(userPort), message);

    // forward to all other users (except the one who sent the message)
    for (int forward_port : users.keySet()) {
        if (forward_port != userPort) {
            DatagramPacket forward = new DatagramPacket(byteMessage, byteMessage.length, address, forward_port);
            try {
                socket.send(forward);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```
- **`saveMessage`**: Este método guarda un mensaje en el archivo `messages.txt`.

```java
private static void saveMessage(String user, String text) {
    String message = user + ": " + text;
    try {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
        bw.write(message);
        bw.newLine();
        bw.close();
    } catch (IOException e) {
        System.err.println("Sum' went wrong persisting");
    }
}
```

- **`validateUser`**: Este método valida un nuevo usuario y gestiona el envío de mensajes de error o confirmación. También puede activar la opción de mostrar mensajes previos.


```java
private static void validateUser(String message, DatagramPacket packet) {
    String[] splitMessage = message.split(" ");
    String error = "This user is currently unavailable, please introduce another nickname";
    String correct = "Username set to: " + splitMessage[1];
    DatagramPacket forward;
    if (users.containsValue(splitMessage[1])) {
        forward = new DatagramPacket(error.getBytes(), error.getBytes().length, address, packet.getPort());
    } else {
        users.put(packet.getPort(), splitMessage[1]);
        forward = new DatagramPacket(correct.getBytes(), correct.getBytes().length, address, packet.getPort());
    }

    if (splitMessage.length > 2) {
        String messages = splitMessage[2];
        previousMessages = messages.equalsIgnoreCase("Y");
    }
    try {
        socket.send(forward);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

- **`showPrevoiusMesssages`**: Este método envía mensajes previos al usuario recién conectado si así lo solicita.

```java
private static void showPrevoiusMesssages(DatagramSocket socket, DatagramPacket packet) {
    DatagramPacket forward;
    try {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                forward = new DatagramPacket(line.getBytes(), line.getBytes().length, address, packet.getPort());
                socket.send(forward);
            }
        } catch (EOFException e) {
            br.close();
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

### Resumen

Este código implementa un servidor de chat UDP que puede manejar múltiples usuarios. Los usuarios pueden conectarse, enviar mensajes, recibir mensajes previos y desconectarse. Los mensajes se guardan en un archivo `messages.txt`, y el servidor se ejecuta indefinidamente, procesando paquetes UDP entrantes y salientes.

**Cliente UDP (`UdpClient.java`)**:
### Descripción General

Este código implementa un cliente UDP en Java que se conecta a un servidor de chat. Utiliza `DatagramSocket` para enviar y recibir mensajes a través de paquetes UDP. El cliente también maneja la inicialización del nombre de usuario y puede cambiar el nombre o salir del chat mediante comandos específicos.


#### Variables Estáticas

```java
private static final DatagramSocket socket;
static Scanner sc = new Scanner(System.in);
private static final InetAddress address;
private static String username;
private static final int SERVER_PORT = 8000;
private static byte[] incoming = new byte[1024];
```

- `socket`: Socket UDP del cliente.
- `sc`: Scanner para leer la entrada del usuario desde la consola.
- `address`: Dirección IP del servidor.
- `username`: Nombre de usuario del cliente.
- `SERVER_PORT`: Puerto del servidor.
- `incoming`: Array de bytes para almacenar los datos entrantes.

#### Inicialización Estática

```java
static {
    try {
        socket = new DatagramSocket(); // init to any available port
    } catch (SocketException e) {
        throw new RuntimeException(e);
    }
}

static {
    try {
        address = InetAddress.getByName("localhost");
    } catch (UnknownHostException e) {
        throw new RuntimeException(e);
    }
}
```

Se inicializan el socket y la dirección del servidor. Si hay algún problema, se lanza una excepción en tiempo de ejecución.

#### Método `main`

```java
public static void main(String[] args) throws IOException {
    System.out.println("Hello!\nWelcome to the best chat ever!\n------------------------ INFO ------------------------\nWhenever you enter the chat you have to put your username and\n" +
    "optionally a Y indicating you want to read previous messages\nCommands:" +
            "\n   /nick 'newUsername':  to change your username\n   /quit:  to exit the chat\n");
    System.out.println("Nickname: ");

    // send initialization message to the server and validate username
    String received;
    do {
        username = sc.nextLine();
        byte[] uuid = ("init; " + username).getBytes();
        DatagramPacket initialize = new DatagramPacket(uuid, uuid.length, address, SERVER_PORT);
        socket.send(initialize);

        DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength()) + "\n";
        System.out.print(received);

    } while (received.equals("This user is currently unavailable, please introduce another nickname\n"));

    String msgSalida;
    ClientThread thread = new ClientThread(socket);
    thread.start();

    do {
        msgSalida = sc.nextLine();
        byte[] sendData = msgSalida.getBytes();
        DatagramPacket salida = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(salida);
    } while (!msgSalida.equals("/quit"));
}
```

Este método principal hace lo siguiente:

1. **Bienvenida y Comandos**: Muestra un mensaje de bienvenida e información sobre los comandos disponibles.
2. **Inicialización del Usuario**: Solicita un nombre de usuario y envía un mensaje de inicialización al servidor para validarlo.
3. **Recepción de Respuesta del Servidor**: Espera la respuesta del servidor para confirmar si el nombre de usuario es válido.
4. **Hilo del Cliente**: Inicia un hilo (`ClientThread`) para manejar la recepción de mensajes.
5. **Bucle de Mensajes**: En un bucle, lee mensajes de la consola y los envía al servidor hasta que el usuario ingresa `/quit`.

#### Resumen

Este código implementa un cliente de chat UDP en Java. El cliente permite a los usuarios ingresar un nombre de usuario y enviar mensajes al servidor. También maneja comandos para cambiar el nombre de usuario y salir del chat. El cliente utiliza un hilo separado para recibir mensajes del servidor y mostrarlos al usuario en la consola.

### Clase `ClientThread`


La clase `ClientThread` es una subclase de `Thread` diseñada para manejar la recepción de mensajes en un cliente de chat utilizando UDP. Su propósito principal es recibir mensajes del servidor y mostrarlos al usuario en la consola. Aquí hay un desglose detallado de su implementación:


#### Variables de Instancia

```java
private DatagramSocket socket;
private byte[] buffer = new byte[256];
```

- `socket`: El socket UDP utilizado para recibir datos.
- `buffer`: Un array de bytes utilizado para almacenar temporalmente los datos recibidos del servidor.

#### Constructor

```java
public ClientThread(DatagramSocket socket) {
    this.socket = socket;
}
```

El constructor toma un objeto `DatagramSocket` como parámetro y lo asigna a la variable de instancia `socket`. Esto permite que el hilo utilice el socket del cliente principal para recibir datos.

#### Método `run`

```java
@Override
public void run() {
    System.out.println("starting thread");
    String message;
    do {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        message = new String(packet.getData(), 0, packet.getLength());
        if (message.equals("exit"))
            System.err.println(message);
        else
            System.out.println(message);
    } while (!message.equals("exit"));
}
```

Este es el método principal del hilo, que se ejecuta cuando se inicia el hilo. Aquí está el desglose paso a paso:

1. **Mensaje de Inicio**: `System.out.println("starting thread");` imprime un mensaje en la consola indicando que el hilo ha comenzado a ejecutarse.

2. **Bucle de Recepción**:
    - **Preparar el Paquete**: `DatagramPacket packet = new DatagramPacket(buffer, buffer.length);` prepara un `DatagramPacket` utilizando el buffer predefinido.
    - **Recibir Paquete**: `socket.receive(packet);` bloquea y espera a que llegue un paquete. Cuando un paquete es recibido, se almacena en `packet`.
    - **Manejar Excepción**: Si ocurre una excepción de E/S, se lanza una `RuntimeException`.

3. **Procesar Mensaje**:
    - **Convertir a String**: `message = new String(packet.getData(), 0, packet.getLength());` convierte los datos del paquete en una cadena de texto.
    - **Verificar y Mostrar Mensaje**: 
        - Si el mensaje es `"exit"`, se imprime un mensaje de error en la consola.
        - Si el mensaje es cualquier otra cosa, se imprime en la consola normalmente.

4. **Condición de Salida**: El bucle continúa hasta que el mensaje recibido sea `"exit"`, lo que indica que el hilo debe terminar.

### Resumen

La clase `ClientThread` se encarga de recibir mensajes del servidor en un cliente de chat UDP. Corre en su propio hilo separado para no bloquear el flujo principal del programa mientras espera mensajes. Utiliza un bucle para recibir mensajes continuamente y los muestra en la consola hasta que recibe un mensaje de "exit". Esto permite que el cliente procese mensajes entrantes de manera eficiente y reactiva.
