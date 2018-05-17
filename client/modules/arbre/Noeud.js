"use strict";
/**
 * "Classe" Noeud : représente un noeud d'un arbre 
 */
define(function () {
  /**
   * Constructeur 
   * 
   * @param {*} options : les options de construction 
   */
  function Noeud(options) {
    this.id = options.id;
    this.label = options.label;
    if (options.parent) {
      this.parent = options.parent;
      this.profondeur = this.parent.profondeur + 1;
      this.parent.enfants.push(this);
    } else {
      this.profondeur = 0;
    }
    if (options.dernier) {
      this.dernier = options.dernier;
    } else {
      this.dernier = false;
    }
    this.contenu = document.createElement("div");
    this.contenu.classList.add("noeud");
    this.plie = true;
    this.enfants = [];
  }

  /**
   * Recherche les ancêtres d'un noeud et les renvoie dans un tableau 
   */
  Noeud.prototype.rechercherAncetres = function () {

    var ancetres = [], plusVieuxParent = this;

    while (plusVieuxParent.parent != undefined) {
      ancetres.push(plusVieuxParent.parent);
      plusVieuxParent = plusVieuxParent.parent;
    }
    //console.log("%s parents trouvés", ancetres.length);
    return ancetres;
  }

  /**
   * Complète l'espace à gauche du noeud (par des images)
   */
  Noeud.prototype.completerImagesArbres = function () {
    /** fonction qui permet de savoir si une colonne d'indice i, dans l'arbre, 
     * doit être représentée par une ligne verticale ou par une image vide */
    function estVide(i, ancetres) {
      var parentPrisEnCompte = ancetres[ancetres.length - i - 1];
      return parentPrisEnCompte.dernier;
    }

    // le décallage des images à droite en fonction de la profondeur : 

    // d'abord on cherche tous les parents : 
    var ancetres = this.rechercherAncetres();

    for (var i = 0; i < this.profondeur; i++) {
      var imageVide = document.createElement("img");
      imageVide.src = "img/s.gif";
      imageVide.classList.add("arbre-img");
      if (!estVide(i, ancetres)) {
        imageVide.classList.add("ligne-verticale");
      }
      this.contenu.appendChild(imageVide);
    }
  }

  /**
   * Calcule et ajoute les classes à un élément, en fonction des attributs du noeud 
   */
  Noeud.prototype.calculerClasses = function (element) {
    element.className = "";

    element.classList.add("arbre-img");
    element.classList.add("actif");

    if (this.plie) {
      if (this.dernier) {
        element.classList.add("arbre-coude-plie-fin");
      } else {
        element.classList.add("arbre-coude-plie");
      }
    } else {
      if (this.dernier) {
        element.classList.add("arbre-coude-deplie-fin");
      } else {
        element.classList.add("arbre-coude-deplie");
      }
    }

  }

  /**
   * Dessiner un noeud dans le DOM 
   */
  Noeud.prototype.dessiner = function () {
    var self = this;
    var labelNoeud = document.createElement("span");
    labelNoeud.innerHTML = self.label;

    this.completerImagesArbres();

    this.bouton = document.createElement("img");
    this.bouton.src = "img/s.gif";
    this.calculerClasses(this.bouton);

    this.bouton.addEventListener("click", self.actionner.bind(this));
    this.contenu.appendChild(this.bouton);

    var icone = document.createElement("img");
    icone.src = "img/folder.png";
    icone.classList.add("arbre-icone-noeud");
    this.contenu.appendChild(icone);

    this.contenu.appendChild(labelNoeud);

    if (this.parent) {
      this.parent.contenu.appendChild(this.contenu);
    } else {
      this.arbre.contenu.appendChild(this.contenu);
    }
  }

  /**
  * Gestion des évènements d'activation / désactivation d'un noeud
  */
  Noeud.prototype.actionner = function () {
    var self = this;
    //console.log("Actionner Noeud " + self.id);

    this.calculerClasses(this.bouton);

    if (this.plie) {
      // console.log("noeud plié - on déplie");
      this.arbre.noeudHandler(this);
    } else {
      //console.log("noeud déplié - on plie");
      this.supprimerEnfants();
    }

    this.plie = !this.plie;
  }

  /**
   * Supprime les enfants du noeud 
   */
  Noeud.prototype.supprimerEnfants = function () {
    this.enfants.forEach(function (enfant) {
      enfant.supprimer();
    })
    this.enfants = [];
  }

  /**
  * Supprime le noeud 
  */
  Noeud.prototype.supprimer = function () {
    while (this.contenu.hasChildNodes()) {
      this.contenu.removeChild(this.contenu.firstChild);
    }
  }

  return Noeud;
});