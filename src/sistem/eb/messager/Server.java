package sistem.eb.messager;



import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

    private int port;
    private List<User> clients;
    private ServerSocket server;

    public static void main(String[] args) throws IOException {
        new Server(1100).run();
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<User>();
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(port) {
                protected void finalize() throws IOException {
                    this.close();
                }
            };
            System.out.println("Port 1100 is now open.");

            while (true) {
                // accepts a new client
                Socket client = server.accept();

                // get nickname of newUser
                String nickname = (new Scanner(client.getInputStream())).nextLine();
                nickname = nickname.replace(",", ""); //  ',' use for serialisation
                nickname = nickname.replace(" ", "_");
                System.out.println("New Client: \"" + nickname + "\"\n\t     Host:" + client.getInetAddress().getHostAddress());

                // create new User
                User newUser = new User(client, nickname);

                // add newUser message to list
                this.clients.add(newUser);

                // Welcome message
                newUser.getOutStream().println(
                        "<h2 style=\"color: #666666;\">Dobrodošao " + newUser.toString() + " u <span style=\"color: #4682b4;\"><strong>PRIČAONICU</strong></span>, pričaj!</h2>"
                );

                // create a new thread for newUser incoming messages handling
                new Thread(new UserHandler(this, newUser)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // delete a user from the list
    public void removeUser(User user) {
        this.clients.remove(user);
    }

    // send incoming msg to all Users
    public void broadcastMessages(String msg, User userSender) {
        for (User client : this.clients) {
            client.getOutStream().println(
                    userSender.toString() + "<span>: " + msg + "</span>");
        }
    }

    // send list of clients to all Users
    public void broadcastAllUsers() {
        for (User client : this.clients) {
            client.getOutStream().println(this.clients);
        }
    }

    // send message to a User (String)
    public void sendMessageToUser(String msg, User userSender, String user) {
        boolean find = false;
        for (User client : this.clients) {
            if (client.getNickname().equals(user) && client != userSender) {
                find = true;
                userSender.getOutStream().println(userSender.toString() + " to " + client.toString() + ": " + msg);
                client.getOutStream().println(
                        "(<b>Private</b>)" + userSender.toString() + "<span>: " + msg + "</span>");
            }
        }
        if (!find) {
            userSender.getOutStream().println(userSender.toString() + " to (<b>no one!</b>): " + msg);
        }
    }
}

class UserHandler implements Runnable {

    private Server server;
    private User user;

    public UserHandler(Server server, User user) {
        this.server = server;
        this.user = user;
        this.server.broadcastAllUsers();
    }

    public void run() {
        String message;

        // when there is a new message, broadcast to all
        Scanner sc = new Scanner(this.user.getInputStream());
        while (sc.hasNextLine()) {
            message = sc.nextLine();

            // smiley
            message = message.replace(":poop:", "<img src='http://3.bp.blogspot.com/-BU52W3FrKWs/UOEeSrJdH9I/AAAAAAAACqc/Rc3vhxTt-OU/s1600/poop-facebook-emoticon.png'>");
            message = message.replace(":putnam:", "<img src='http://4.bp.blogspot.com/-anHsuQ9aTPk/UZPEJVmTJUI/AAAAAAAADuc/vQr8wSXbeEI/s1600/putnam-emoticon.png'>");
            message = message.replace(":)", "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
            message = message.replace(":42:", "<img src='http://3.bp.blogspot.com/_o0z7HPmXX6w/S3v9RWpcMkI/AAAAAAAABdQ/0MnQnSVL5lQ/s400/42facebookchatemoticon.gif'>");
            message = message.replace(":D", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
            message = message.replace(":d", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
            message = message.replace(":(", "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
            message = message.replace("-_-", "<img src='http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png'>");
            message = message.replace(";)", "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
            message = message.replace(":P", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
            message = message.replace(":p", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
            message = message.replace(":o", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
            message = message.replace(":O", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
            message = message.replace("o.O", "<img src='http://3.bp.blogspot.com/-UrCVd-uHlgY/UZFSnLF4K1I/AAAAAAAADoY/dIzdwC608pY/s1600/confused-emoticon-wtf-symbol-for-facebook.png'>");
            message = message.replace(":'(", "<img src='http://1.bp.blogspot.com/-JdQ0M7bFgoE/TviqyPmOJPI/AAAAAAAAAhs/qTvew9Rt0YE/s400/angel%2Bemoticon.png'>");
            message = message.replace("O:-)", "<img src='http://1.bp.blogspot.com/-JdQ0M7bFgoE/TviqyPmOJPI/AAAAAAAAAhs/qTvew9Rt0YE/s400/angel%2Bemoticon.png'>");
            message = message.replace("3:-)", "<img src='http://3.bp.blogspot.com/-h2ErlFczszQ/TvirzZanEUI/AAAAAAAAAh4/rghgvQxSXmQ/s400/devil%2Bemoticon.png'>");
            message = message.replace(">:-O", "<img src='http://1.bp.blogspot.com/-65dTk9XP7ig/Tv9IpQP6kdI/AAAAAAAAAkU/mmIkqHRAOmU/s400/angry%2Bemoticon.png'>");
            message = message.replace(":*", "<img src='http://4.bp.blogspot.com/-iadOX6ehRnw/TvjNY8KWmJI/AAAAAAAAAiQ/Jg6rpG5_r_Y/s400/kiss%2Bemoticon.png'>");
            message = message.replace(":-*", "<img src='http://4.bp.blogspot.com/-iadOX6ehRnw/TvjNY8KWmJI/AAAAAAAAAiQ/Jg6rpG5_r_Y/s400/kiss%2Bemoticon.png'>");
            message = message.replace("<3", "<img src='http://3.bp.blogspot.com/-yH4Y8RvQvqc/TvjOQgHW6eI/AAAAAAAAAic/X5vEH21CXEA/s400/heart%2Bemoticon.png'>");
            message = message.replace("^_^", "<img src='http://4.bp.blogspot.com/-5BFO3PDratE/Tv9GFxyXbXI/AAAAAAAAAj8/xjYxSRsI3IU/s400/kiki%2Bemoticon.png'>");
            message = message.replace("8-)", "<img src='http://3.bp.blogspot.com/-C7L9OMu5rhM/Tv5dKdJVMAI/AAAAAAAAAjA/EzD-Kexh39o/s400/glasses%2Bemoticon.png'>");
            message = message.replace("8|", "<img src='http://2.bp.blogspot.com/-qODY1kxipZ0/Tv5dwDFFntI/AAAAAAAAAjM/cLXT6KEp-bE/s400/sunglasses%2Bemoticon.png'>");
            message = message.replace("(^^^)", "<img src='http://2.bp.blogspot.com/-znxWwMOlNCQ/Tv9DCSAbEDI/AAAAAAAAAjw/fqnxvczUEWw/s400/shark%2Bemoticon.gif'>");
            message = message.replace(":|]", "<img src='http://1.bp.blogspot.com/-HldOzFvQqBk/Tv9J89QYn3I/AAAAAAAAAkg/bSMThZQM8E4/s400/robot%2Bemoticon.gif'>");
            message = message.replace(":v", "<img src='http://4.bp.blogspot.com/-A60R7IeDPnw/Tv9Kv3Z3oMI/AAAAAAAAAks/rxLS0HG7RiY/s400/pacman%2Bemoticon.png'>");
            message = message.replace(">:-(", "<img src='http://4.bp.blogspot.com/-UQmtbPpWsiw/Tv9Nv9EzEhI/AAAAAAAAAk4/wR5VC0VN4B4/s400/grumpy%2Bemoticon.png'>");
            message = message.replace(":-/", "<img src='http://2.bp.blogspot.com/-4G-jNG9Yr3s/Tv9Rrz9lMJI/AAAAAAAAAlc/f2PmgzhpYds/s400/unsure%2Bemoticon.png'>");
            message = message.replace(":3", "<img src='http://4.bp.blogspot.com/-i3GSy3WpYfs/Tv9QrweHOVI/AAAAAAAAAlQ/YBbHR7CdQb4/s400/curly%2Blips%2Bemoticon.png'>");
            message = message.replace("(y)", "<img src='http://3.bp.blogspot.com/--h4eLCX9klE/UZHjEBYfY2I/AAAAAAAADoo/W5bcUQjtXls/s1600/thumb-up-facebook-emoticon-like-symbol.png'>");
            message = message.replace("<(v)", "<img src='http://3.bp.blogspot.com/-dtM2qSBfwZc/UZPES7Ff7rI/AAAAAAAADuk/k-_bsldBLzA/s1600/facebook-penguine-emoticon.png'>");
           
            //gifs 
             message = message.replace("!hehe", "<img src='https://c.tenor.com/E-cLzdlc_icAAAAM/snoopy-giggle.gif'>");
              message = message.replace("!haha", "<img src='https://c.tenor.com/9icBPDvM6cMAAAAM/jerry-funny.gif'>");
              message = message.replace("!dance", "<img src='https://c.tenor.com/_xcWWAZPKwsAAAAS/gif.gif'>");
              message = message.replace("!fall", "<img src='https://c.tenor.com/wsbmWYxnJYQAAAAM/kermit-falling.gif'>");
              message = message.replace("!flight", "<img src='https://c.tenor.com/Lvu0VdlZjn0AAAAM/tom-flying-tom-cat-flying.gif'>");
              message = message.replace("!sleepy", "<img src='https://c.tenor.com/LXjDoTCYWAsAAAAM/sleepy-tom.gif'>");      
              message = message.replace("!omg", "<img src='https://c.tenor.com/dIJvqCnIH1EAAAAM/incr%C3%ADvel-incredible.gif'>");   
             message = message.replace("!love", "<img src='https://c.tenor.com/hCB_3ZAAracAAAAM/hearts-love.gif'>");   
             message = message.replace("!food", "<img src='https://c.tenor.com/0zLNOqdqi2AAAAAM/pantera-c%C3%B4r-rosa-pink-panther.gif'>");    
             message = message.replace("!girlpower", "<img src='https://c.tenor.com/ugHueSht6c0AAAAM/popeye-olive-oil.gif'>"); 
             message = message.replace("!tell", "<img src='https://c.tenor.com/kFypNTd0P2QAAAAM/wimpy-popeye.gif'>"); 
             message = message.replace("!power", "<img src='https://c.tenor.com/vphR8L-gm5kAAAAM/popeye-sailor.gif'>");
            
                    
            // Gestion des messages private
            if (message.charAt(0) == '@') {
                if (message.contains(" ")) {
                    System.out.println("private message : " + message);
                    int firstSpace = message.indexOf(" ");
                    String userPrivate = message.substring(1, firstSpace);
                    server.sendMessageToUser(
                            message.substring(
                                    firstSpace + 1, message.length()
                            ), user, userPrivate
                    );
                }

                // Gestion du changement
            } else if (message.charAt(0) == '#') {
                user.changeColor(message);
                // update color for all other users
                this.server.broadcastAllUsers();
            } else {
                // update user list
                server.broadcastMessages(message, user);
            }
        }
        // end of Thread
        server.removeUser(user);
        this.server.broadcastAllUsers();
        sc.close();
    }
}

class User {

    private static int nbUser = 0;
    private int userId;
    private PrintStream streamOut;
    private InputStream streamIn;
    private String nickname;
    private Socket client;
    private String color;

    // constructor
    public User(Socket client, String name) throws IOException {
        this.streamOut = new PrintStream(client.getOutputStream());
        this.streamIn = client.getInputStream();
        this.client = client;
        this.nickname = name;
        this.userId = nbUser;
        this.color = ColorInt.getColor(this.userId);
        nbUser += 1;
    }

    // change color user
    public void changeColor(String hexColor) {
        Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
        Matcher m = colorPattern.matcher(hexColor);
        if (m.matches()) {
            Color c = Color.decode(hexColor);
            // if the Color is too Bright don't change
            double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue(); // per ITU-R BT.709
            if (luma > 160) {
                this.getOutStream().println("<b>Color too bright, try again!</b>");
                return;
            }
            this.color = hexColor;
            this.getOutStream().println("<b>Color changed successfully.</b> " + this.toString());
            return;
        }
        this.getOutStream().println("<b>Failed to change color.</b>");
    }

    public PrintStream getOutStream() {
        return this.streamOut;
    }

    public InputStream getInputStream() {
        return this.streamIn;
    }

    public String getNickname() {
        return this.nickname;
    }

    // print user with his color
    public String toString() {

        return "<u><span style='color:" + this.color
                + "'>" + this.getNickname() + "</span></u>";

    }
}

class ColorInt {

    public static String[] mColors = {
        "#3079ab", // dark blue
        "#4682B4", // steel blue
        "#5F9EA0", // cadet blue
        "#6495ED", // cornflower blue
        "#6f91fe", // light steel blue
        "#87CEEB", // sky blue
        "#1E90FF", // dodger blue 
        "#87CEFA", // light sky blue
        "#FFD700", // yellow
        "#F0E68C", // khaki
        "#faf878", // red
        "#fada5e", // green
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
