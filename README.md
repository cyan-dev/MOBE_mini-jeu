# MOBE Mini jeu : "Yarrrrh!"

> Un petit jeu de pirate qui cherche des trésors et se bat contre d'autres pirates.  
> #simple #runner #arcade #score

**Groupe** : Saucisse!  
**Membres** : Cyril Esclassan, Dylan Caron, Florian Azizen  
**Capteurs** : Microphone, Boussole, Tactile  
**Dépôt GitHub** : https://github.com/cyan-dev/MOBE_mini-jeu

## Installation

### Via l'APK

Nous avons fait une APK de l'application, celle ci est disponible dans l'archive déposé sur Moodle ou dans la partie Release sur GitHub.

### Via Android Studio

Il vous suffit de cloner le projet et de build avec Android Studio.

## Description de l'application

### Ecran 1 : Main Menu

De haut en bas :

- Nom du jeu
- Meilleur score
- Dernier score
- Sprite du batteau
- Bouton Play

### Ecran 2 : Navigation

De haut en bas :

- Barre de points de vie restants
- Pièces d'or (Score)
- Caps (Direction actuelle et caps tribord/bâbord)
- Curseur (Indique la direction à prendre)
- Batteau (Sprite dynamique)
- Boussole (Permet de changer de cap)

Le bateau avance à vitesse constante vers un des huit caps (Nord, Nord Est, Est, Sud Est, Sud, Sud Ouest, Ouest, Nord Ouest).

Un trésor apparait sur un cap, il faut aller le chercher pour marquer des points. Dés qu'un trésor est récupéré, un nouveau se génère.

On peut temporairement augmenter la vitesse du bateau en soufflant pour gonfler les voiles ou en criant pour motiver l'équipage.  
**Capteur** : Microphone

Un appuis prolongé ou un tapotement sur le bouton "Boussole" permet d'orienter le bateau selon le sens de la boussole du périphérique.  
**Capteur** : Boussole

Des combats se déclenchent aléatoirement et régulièrement.

### Ecran 3 : Fight

De haut en bas :

- Barre de points de vie restants
- Pièces d'or (Score)
- Batteau (Sprite dynamique)
- Batteau ennemi (Sprite dynamique)
- Barre de points de vie de l'ennemi
- Barre/Bouton de chargement des cannons

L'ennemi tire régulièrement et retire une quantité fixe de points de vie.

Pour tirer il faut maintenir le bouton de chargement des canons. Lorsqu'il atteint 100%, le chargement rebondit et se décrémente. Le but est de charger son coup au maximum et de lâcher au bon moment.
**Capteur** : Tactile

Chaque tir de notre batteau produit une petite vibration.

Si la barre de points de vie du batteau tombe à zero, c'est la fin de la partie. Retour au menu principal.

Si la barre de points de vie de l'ennemi tombe à zero, on gagne le combat, cela augmente notre score. Retour à la navigation.

## Points supplémentaires

Le score et les points de vie sont conservés et transmits grace aux `shared preferences`.

Une demande d'accès au microphone apparait à la première ouverture de l'application.

Tous les sprites et SFXs sont libres de droit, nous les avons conçu spécialement pour le projet.
