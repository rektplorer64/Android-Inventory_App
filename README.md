
<div align="center">

<img width="128" height="128" src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="logo">

<span>

`SIDE  PROJECT`

</span>

# Generic Inventory App

<p>A simple Android application that help you remember your belongings!</p>
</div>

## 🚩 Motivations
After finishing the first year of study at Mahidol University, the Faculty of ICT, I was equipped with knowledge and 
skills in Java programming language and desire to put Android knowledge from self-study (via Udacity) to a better use. 
I decided to create an application that allows me to understand the underlying Android application framework even more.

The application that I choose to build is a simple personal inventory manager.  

## 😀 Features
- [x] Persistent local inventory database via an early version of Room Database
- [x] Image Storage for each item
- [x] Item tagging by a color or terms 
- [x] Responsive layout
    - Dual-pane for tablet
    - Single-pane for vertical form-factor devices and phones
- [x] Customizable item list view (Grid/List)
- [x] Detailed item search and filters
    - Search by
        1. Keyword in either item name/description/ID
        2. Date Created
        3. Date Modified
        4. Has any image or not
        5. Quantity of the item
        6. Tags
    - Sort by
        1. Item ID
        2. Item Name
        3. Item Description
        4. Date Created
        5. Date Modified
        6. Color Accent
        7. Quantity
        8. Rating
- [x] User Reviewing (Mimicked)
- [ ] ~~Online Data Synchronization~~ Out of the project scope during that time

## 🖼 Screenshots
There are two panes that uses the screen real-estate efficiently. 
![There are two panes that uses the screen real-estate efficiently.](/previews/images/1.png)

The item list can be changed in terms of the appearance, and the number of columns.
| | | |
|:-------------------------:|:-------------------------:|:-------------------------:|
|<img width="1604" alt="Compact List view" src="/previews/images/2.png"> |  <img width="1604" alt="Small List view" src="/previews/images/3.png">| <img width="1604" alt="Full image height List view" src="/previews/images/4.png">|

This page also support vertical layout.
![Vertical List](/previews/images/5.png)

There are also another page for Search and Filter! You can also change the layout too. Just like the main list page! If you look carefully, you will see that the search keyword is being made bold in the result list.
| One-column Search Results | Two-column Search Results |
|:-------------------------:|:-------------------------:|
|<img width="1604" alt="Search page with 1 list column" src="/previews/images/6.png"> | <img width="1604" alt="Search page with 2 list columns" src="/previews/images/6-1.png"> | 

## 📽 Demos
![Item List](/previews/videos/1-item_list.gif)
![Item rating](/previews/videos/2-rate_comment.gif)
![Image Persistence](/previews/videos/3-image_persistence.gif)
![Item Detail Editing](/previews/videos/3-item_detail_editing.gif)
![Screen Rotation](/previews/videos/5-screen_rotation.gif)
![Searching UI](/previews/videos/6-search.gif)


## Implementation

DISCLAIMER: This app has no clean architecture whatsoever because IT IS MY FIRST APP. Plus, during that time, there were literally no design guideline recommended by Google available.

1. AsyncTask - for item search and sorting in the background thread
2. Material Design Component - Most of the UI elements are based on Google's Material Design.
3. RecyclerView 
    - There are multiple types of RecyclerViewAdapter that are implemented.
    - To change the list item layout at runtime (e.g. Full-height card to small card), there are nasty method calls that force the layout to be redrawn that could lead to performance impacts.
    - During the time of development, multi-selection in RecyclerView needed to be implemented manually. Therefore, there might be some glitches.
4. Fragment - They allow some layouts to be reused from time-to-time. For example, the item detail modal (pop-up) when clicking an item in a search result.
    - It is used to implement Circular revealing User Interface.
    - It allows me to create a dual-pane User Interface layout for the tablet form-factor because I do not have to have redundant part of source code for different layouts.
    - Manual Fragment Transaction is used heavily in this project because there were not any abstraction provided by the framework.
5. Preference - For the search and filter page, the search and filter settings that user has adjusted are automatically saved and restored for the user.
6. Room Database - Basically, SQLite used internally in an Android application to store data persistently on disk. 
    - I actually designed a simple relational model for this app too.
    - The database is lazily initialized when the app runs for the first time. Data will be randomly generated at first launch.
 