/**
 * Contains the names of all the planets and the defintion of the colors
 */

package spacesim;

import java.awt.Color;
import java.util.HashMap;

public class Names {
    public static String[] names = new String[]{"Fatima", 
            "Madalyn", "Kayleigh", "Corbin", "Shelby", "Harper", "Maddox", "Leah", "Holly", "Kellen", "Nadia", 
            "Bailey", "Isla", "Alana", "Leo", "Rylie", "Angelina", "Silas", "Ella", "Elle", "Ronald", 
            "Dominick", "Ernesto", "Kolton", "Easton", "Arturo", "Lorelei", "Jayleen", "Titus", "Albert", "Calvin", 
            "Declan", "Elaina", "Eleanor", "Jaime", "Lyla", "Henry", "Xavier", "Sebastian", "Jayda", "Brock", 
            "Joshua", "Charlotte", "Thomas", "Parker", "Jillian", "Bentley", "Maurice", "Keith", "Carly", "Jazmin", 
            "Jayce", "Mateo", "Kathryn", "Kaylee", "Alessandra", "Fiona", "Paul", "Cesar", "George", "Kevin", 
            "Chase", "Tatiana", "Gustavo", "Gia", "Martin", "Willow", "Sage", "Ryleigh", "Darren", "Matthew", 
            "Ariel", "Desmond", "Jane", "Bryson", "Lexi", "Alina", "Zion", "Jessica", "Avery", "Giovanni", 
            "Orion", "Hailey", "Tucker", "Cruz", "Reed", "Yahir", "Peyton", "Jalen", "Arabella", "Damian", 
            "Charlie", "Amaya", "Arthur", "Archer", "Kamila", "Kristopher", "David", "Stephanie", "Alyssa", "Saul", 
            "Lauren", "Dean", "Kellan", "Cynthia", "Clara", "Jennifer", "Rebekah", "Travis", "Zander", "Maximilian", 
            "Hayley", "Iker", "Grayson", "Naomi", "Izaiah", "Victor", "Devin", "Blake", "Oscar", "Jameson", 
            "Robert", "Mathew", "Alyson", "Guadalupe", "Nayeli", "Alberto", "Isaac", "Daniela", "Kira", "Breanna", 
            "Caitlyn", "Casey", "Lena", "Cora", "Giselle", "June", "Katelyn", "Kaydence", "Leilani", "Malcolm", 
            "Katherine", "Connor", "Rocco", "Amber", "Lane", "Aria", "Erick", "Kiley", "Kennedy", "Eva", 
            "Tanner", "Zachary", "Orlando", "Zayden", "Wyatt", "Claire", "Jimmy", "Kennedi", "Aden", "Gerardo", 
            "Iris", "Savanna", "Daniella", "Tiffany", "Talia", "Alivia", "Chance", "Luna", "Tessa", "Nicole", 
            "Makenna", "Owen", "Lia", "Brayan", "Sara", "Jerry", "Layla", "Brian", "Nora", "Rowan", 
            "Ximena", "Katelynn", "Nyla", "Walter", "Erin", "Paisley", "Tristan", "Raymond", "Phoenix", "Brynn", 
            "Kai", "Molly", "Janelle", "Chandler", "Aaliyah", "Clayton", "Jordan", "Jack", "Miranda", "Tyson", 
            "Jace", "Aileen", "Keaton", "Barrett", "Mariah", "Dylan", "Nathan", "Lucas", "Natalia", "Joel", 
            "Matteo", "Jeffrey", "Alexis", "Dante", "Emanuel", "Roman", "Athena", "Reese", "Daisy", "Kelly", 
            "Sophie", "Briana", "Alaina", "Carlos", "Amare", "Crystal", "Dillon", "Maxwell", "Alayna", "Makayla", 
            "Brennan", "Isabel", "Logan", "Madilyn", "Leslie", "Sean", "Quinn", "Beau", "Caden", "Karter", 
            "Zachariah", "Eliana", "Gabriela", "Marshall", "Jax", "Marissa", "Johnathan", "Kinley", "Simon", "Evangeline", 
            "Elias", "Teagan", "Cooper", "Muhammad", "Maddison", "Trinity", "Benjamin", "Addison", "Marco", "Delilah", 
            "Cecilia", "Serena", "Kelsey", "Drew", "Wesley", "Elijah", "Evan", "Abram", "Adalynn", "Helen", 
            "Brooke", "Luis", "Grace", "Jayden", "Enzo", "Hugo", "Ashley", "Anaya", "Colton", "Brooklynn", 
            "Brody", "Seth", "Kyle", "Macie", "Addyson", "Maximus", "Adam", "Adan", "Theodore", "Dominic", 
            "Gunner", "Rafael", "Karina", "Summer", "Elliot", "Sam", "Khalil", "Savannah", "Emmanuel", "Aaron", 
            "Kylie", "Michaela", "Aubrie", "Mila", "Milo", "Abel", "Liam", "Kate", "Esmeralda", "Destiny", 
            "Faith", "Caiden", "Rose", "Eliza", "Kamden", "Joseph", "Derek", "Harmony", "Danica", "Dayana", 
            "Caroline", "Mitchell", "Cadence", "Carolina", "Armando", "Elizabeth", "Aubrey", "Everett", "Samuel", "Jimena", 
            "Romeo", "Juliette", "Journey", "Ricky", "Joe", "Aubree", "Josie", "Josiah", "Valeria", "Raelynn", 
            "Bryce", "Valerie", "Reece", "Jude", "Emery", "Nina", "Miguel", "Ryan", "Kinsley", "Allen", 
            "Lilyana", "Melody", "Ayla", "Zackary", "Aylin", "Josephine", "Julian", "Juliana", "Kendra", "Jesus", 
            "Gianna", "Fabian", "Grady", "Erik", "Eduardo", "Zariah", "Bruce", "Eric", "Adelaide", "Nicolas", 
            "Kiera", "Kaleb", "Bridget", "Brylee", "Jaxon", "Megan", "Briella", "Axel", "Jasmin", "Genesis", 
            "Cheyenne", "Anthony", "Johan", "Jeremiah", "Shane", "Brielle", "Lucy", "Ruben", "Miriam", "Nevaeh", 
            "Levi", "Keira", "Raul", "Leonel", "Rebecca", "Kaiden", "Lincoln", "Javier", "Justin", "Aniyah", 
            "Patrick", "Phillip", "Paola", "Miracle", "Miles", "Serenity", "Malik", "Miley", "Kaylin", "Malia", 
            "Cristian", "Selena", "Ali", "Caleb", "Anabelle", "Zoey", "Alejandro", "Colin", "Liliana", "Elise", 
            "Kylee", "Jamison", "Cody", "Angelo", "Diego", "Oliver", "Richard", "Marcos", "Kyra", "Angela", 
            "Kimberly", "Gavin", "Giovani", "Larry", "Kiara", "Anya", "Ainsley", "Allyson", "Christian", "Sadie", 
            "Hannah", "Adeline", "Alicia", "Jasmine", "Nia", "Lucia", "Estrella", "Adalyn", "Brendan", "Kaelyn", 
            "Mikayla", "Dulce", "Addisyn", "Julius", "Lillie", "Ryder", "Allison", "Luca", "Heidi", "Ivan", 
            "Ayden", "Paige", "Leland", "Angelique", "Colten", "Trenton", "Aidan", "Celeste", "Sofia", "Kieran", 
            "Kayden", "Tristen", "Donald", "Esteban", "Lucille", "Gabriella", "Braden", "Gabrielle", "Gracie", "Janiyah", 
            "Lola", "Ian", "Paris", "Karla", "Ismael", "Davis", "Genevieve", "Walker", "Skylar", "Randy", 
            "Alan", "Stephen", "Lilly", "Kassidy", "Anna", "Russell", "Jakob", "Jaden", "Santiago", "Mauricio", 
            "Vanessa", "Chelsea", "Gemma", "Jay", "Natalie", "Bianca", "Giuliana", "Nylah", "Natasha", "Khloe", 
            "Bryant", "Michelle", "Brayden", "Brenden", "Lillian", "Louis", "Brittany", "Stella", "Adriana", "Paxton", 
            "River", "Amir", "Angel", "Felix", "Mackenzie", "Tenley", "Camden", "Aniya", "Dalton", "Colby", 
            "Ryland", "Melany", "Jackson", "Zoe", "Grant", "Kyler", "Jaelyn", "Kali", "Bella", "Lilah", 
            "Anderson", "Lilian", "Malachi", "Madeline", "Sydney", "John", "Scarlett", "Zaiden", "Issac", "Callie", 
            "Aurora", "Ariana", "Jonah", "Kaden", "Yaretzi", "Jonas", "Rhys", "Kason", "Gracelyn", "Isaiah", 
            "Emiliano", "King", "Micah", "Viviana", "Isabelle", "Braylen", "Cash", "Gary", "Edgar", "Trey", 
            "Derrick", "Phoebe", "Annie", "Gideon", "Prince", "Maggie", "Veronica", "Peter", "Jason", "Armani", 
            "Ivy", "Laura", "Karen", "Lindsey", "Gael", "Violet", "Dawson", "Alejandra", "Graham", "Mohamed", 
            "Timothy", "Lila", "Skyler", "Zane", "Collin", "Hadley", "Londyn", "Kyla", "Brooks", "Norah", 
            "Lily", "Dane", "Riley", "Mekhi", "Atticus", "Myles", "Mariana", "Amanda", "Ty", "Macy", 
            "Spencer", "Brooklyn", "Cullen", "Amari", "Ashlyn", "Jaxson", "Maci", "Remington", "Georgia", "Lukas", 
            "Trevor", "Bethany", "Emilee", "Donovan", "Rodrigo", "Alexandra", "Vincent", "Caylee", "Francisco", "Camille", 
            "Haylee", "Elliana", "Ana", "Kenneth", "Cali", "Cassandra", "Colt", "Rachel", "Reagan", "Julissa", 
            "Sienna", "Emely", "Harrison", "Trent", "Kobe", "Cole", "Myla", "Allie", "Melissa", "Adriel", 
            "Braxton", "Jacoby", "Edwin", "Piper", "Skye", "Kade", "Brianna", "Ellie", "Danielle", "Courtney", 
            "Mckinley", "Elliott", "Alexia", "Nathaniel", "Hope", "Josue", "Jacqueline", "Israel", "Esther", "Eloise", 
            "Andre", "Luke", "Hunter", "Camryn", "Brantley", "James", "Amelia", "Ashlynn", "Tatum", "Emerson", 
            "Marcus", "Danny", "Monica", "Jocelyn", "Keegan", "Kenley", "Reid", "Arianna", "Bristol", "Jared", 
            "Manuel", "Curtis", "Danna", "Carmen", "Angie", "Julio", "Finley", "Carter", "Braydon", "Brycen", 
            "Karson", "Evelyn", "Julie", "Julia", "Kara", "Priscilla", "Max", "Dennis", "Angelica", "Omar", 
            "Sarah", "Sarai", "Griffin", "Quentin", "Vivienne", "Landon", "Kaitlyn", "Jorge", "Rylan", "Ricardo", 
            "Liana", "Hayden", "Tyler", "Lorenzo", "Emilia", "Jeremy", "Kamryn", "Nicholas", "Maverick", "Porter", 
            "Damon", "Vivian", "Xander", "Samantha", "Camila", "Victoria", "Darius", "Margaret", "Devon", "Christopher", 
            "Mckenzie", "Olive", "Philip", "Kyleigh", "Adrianna", "Jett", "Ronan", "Jayla", "Gabriel", "Dexter", 
            "Roberto", "Alice", "Drake", "Sawyer", "Solomon", "Baylee", "Emilio", "Madeleine", "Kailyn", "Braylon", 
            "Mallory", "Alexandria", "Jaylen", "Cohen", "Sergio", "Ezekiel", "Penelope", "Scarlet", "Izabella", "London", 
            "Jasper", "Steven", "Mya", "Audrina", "Dustin", "Beckett", "Nico", "Weston", "Jamari", "Garrett", 
            "Pierce", "Brandon", "Francesc", "Andy", "Noel", "Delaney", "Jonathan", "Mary", "Kailey", "Holden", 
            "Bradley", "Lance", "Frank", "Annabella", "Landyn", "Troy", "Annabelle", "Kayla", "Raegan", "Jayson", 
            "Chris", "Ibrahim", "Lacey", "Ruby", "Abby", "Cade", "Heaven", "Lilliana", "Brady", "Eden", 
            "Finn", "Makenzie", "Antonio", "Dakota", "Leila", "Corey", "Elsie", "Kadence", "Cayden", "Imani", 
            "Joaquin", "Payton", "Leonardo", "August", "Kendall", "Ezra", "Taylor", "Jazmine", "Johnny", "Jade", 
            "Valentina", "Catherine", "Jada", "Jaiden", "Autumn", "Cameron", "Marley", "Katie", "Julianna", "Eli", 
            "Uriel", "Dallas", "Juan", "Madelynn", "Gage", "Rylee", "Caitlin", "Ada", "Joselyn", "Ruth", 
            "Gregory", "Abraham", "Alexa", "Tiana", "Lawrence", "Shawn", "Itzel", "Waylon", "Jamie", "Greyson", 
            "Melanie", "Amy", "Sasha", "Lydia", "Jazlyn", "Presley", "Daphne", "Alondra", "Elena", "Sabrina", 
            "Bryan", "Mikaela", "Maliyah", "Kaylie", "Anastasia", "Cassidy", "Adrian", "Pedro", "Ashton", "Mark", 
            "Austin", "Diana", "Nikolas", "Preston", "Bennett", "Haley", "Maximiliano", "Leon", "Kameron", "Eddie", 
            "Aliyah", "Joanna", "Tony", "Kenzie", "Enrique", "Jenna", "Alison", "Jesse", "Jose", "Kingston", 
            "Madisyn", "Judah", "Tate", "Hudson", "Braelyn", "Christina", "Noelle", "Carson", "Hector", "Edward", 
            "Camilla", "Conner", "Sierra", "Amiyah", "Madelyn", "Audrey", "Jake", "Maya", "Lana", "Aleah", 
            "Lexie", "Andres", "Hazel", "Jordyn", "Andrew", "Finnegan", "Braeden", "Mario", "Nolan", "Alijah", 
            "Landen", "Scott", "Knox", "Andrea", "Laila", "Maria", "Fernanda", "Alex", "Hanna", "Damien", 
            "Fernando", "April", "Nehemiah", "Morgan", "Alfredo", "Emmett", "Erica", "Mckenna", "Alec", "Asher", 
            "Jaliyah", "Moises", "Charles", "Ryker", "Adelyn", "Lyric", "Marvin", "Pablo", "Harley", "Juliet", 
            "Charlee", "Brynlee"};
    
    public static HashMap<String, Color> generateColors() {
        HashMap<String, Color> colors = new HashMap<>();
        colors.put("Black", Color.BLACK);
        colors.put("Blue", Color.BLUE);
        colors.put("Yellow", Color.YELLOW);
        colors.put("Orange", Color.ORANGE);
        colors.put("Green", Color.GREEN);
        colors.put("Red", Color.RED);
        colors.put("Gray", Color.GRAY);
        colors.put("Pink", Color.PINK);
        colors.put("Cyan", Color.CYAN);
        colors.put("Magenta", Color.MAGENTA);
        return colors;
    }
}
