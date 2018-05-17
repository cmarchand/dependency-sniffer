"use strict";
/**
 * "Classe" Feuille : représente un noeud de l'arbre qui n'a pas d'enfant. 
 * 
 * "Hérite" de la "Classe" Noeud
 */
define(['modules/arbre/Noeud'], function (Noeud) {
  /**
   * Constructeur 
   * Appelle le constructeur de Noeud
   * 
   * @param {*} options : les options de construction 
   */
  function Feuille(options) {
    Noeud.call(this, options);
    this.active = options.active;
  }

  // Chainage de prototype : 
  Feuille.prototype = Object.create(Noeud.prototype);

  /**
   * Dessiner une feuille dans le DOM 
   */
  Feuille.prototype.dessiner = function () {
    var self = this;
    var labelNoeud = document.createElement("span");
    labelNoeud.innerHTML = self.label;

    // toutes les feuilles ne sont pas actives : on n'a pas forcément de listener 
    if (self.active) {
      labelNoeud.classList.add("feuille-active");
      labelNoeud.addEventListener("click", self.actionner.bind(this));
    }

    this.completerImagesArbres();

    var fauxBouton = document.createElement("img");
    fauxBouton.src = "img/s.gif";
    fauxBouton.classList.add("arbre-img");
    if (this.dernier) {
      fauxBouton.classList.add("feuille-cadet");
    } else {
      fauxBouton.classList.add("feuille-non-cadet");
    }

    this.contenu.appendChild(fauxBouton);

    var icone = document.createElement("img");
    icone.src = "img/s.gif";
    icone.classList.add("arbre-img");
    icone.classList.add("arbre-icone-feuille");
    this.contenu.appendChild(icone);

    this.contenu.appendChild(labelNoeud);

    if (this.parent) {
      this.parent.contenu.appendChild(this.contenu);
    } else {
      this.arbre.contenu.appendChild(this.contenu);
    }
  }

  /**
   * Gestion de l'évènement d'activation de la feuille
   */
  Feuille.prototype.actionner = function () {
    var self = this;
    //console.log("Actionner Feuille " + self.id);
    this.arbre.feuilleHandler(this);
  }


  return Feuille;
});