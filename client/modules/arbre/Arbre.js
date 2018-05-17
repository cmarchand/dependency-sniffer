"use strict";
/**
 * "Classe" Arbre : représente l'arbre
 */
define(['modules/arbre/Noeud', 'modules/arbre/Feuille'], function(Noeud, Feuille) {
	  /**
   * Constructeur 
   * 
   * @param {*} contenu : l'élément qui contiendra l'arbre
   */
  function Arbre(contenu) {
		this.contenu = contenu;
		this.noeuds = [];
	}

	/**
	 * Ajoute un noeud 
	 */
	Arbre.prototype.ajouterNoeud = function (options) {
		var noeud = new Noeud(options);
		noeud.arbre = this;
		this.noeuds.push(noeud);
		noeud.dessiner();
	}
	/**
	 * Ajoute une feuille  
	 */
	Arbre.prototype.ajouterFeuille = function (options) {
		var noeud = new Feuille(options);
		noeud.arbre = this;
		this.noeuds.push(noeud);
		noeud.dessiner(options.active);
	}

	/**
	 * Supprime un noeud 
	 */
	Arbre.prototype.supprimerNoeud = function (idNoeud) {

		this.noeuds.forEach(function (noeud, index) {
			if (idNoeud === noeud.id) {
				this.noeuds.splice(index, 1);
			}
		});
  }
	/**
	 * Supprime tous les noeuds 
	 */
  Arbre.prototype.supprimerNoeuds = function () {
    this.noeuds.length = 0;
    while (this.contenu.hasChildNodes()) {
      this.contenu.removeChild(this.contenu.firstChild);
    }
  }
  
  return Arbre;
});