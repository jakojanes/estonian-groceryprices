# Estonian Grocery Shop Web Scraper (Java)

This Java-based web scraper is designed to collect product data from all major grocery shops in Estonia, including Prisma, Maxima, Selver, Coop, and Rimi. It saves the scraped data in JSON format for easy access and analysis.


## Prerequisites

Before using this web scraper, make sure you have the following prerequisites set up:

- Java Development Kit (JDK) installed on your system.
- Gradle build tool installed (https://gradle.org/).
- Chromedriver (included in the repo)

## Usage

1. Clone this repository to your local machine:

  
   git clone https://github.com/jakojanes/webscraper-groceryprices.git
   cd webscraper-groceryprices
   gradle shadowJar
   java -jar <jar-file-name>.jar
