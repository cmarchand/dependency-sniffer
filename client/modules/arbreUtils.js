"use strict";
/**
 * Module utilitaire pour la gestion de l'arbre 
 * 
 */
define(["modules/arbre/Arbre"], function (moduleArbre) {

	/**
	 * Création d'un arbre 
	 * 
	 * @param {*} conteneur : l'élément du DOM dans lequel sera ajouté l'arbre
	 */
	function creerArbre(conteneur) {
		//console.log("Création d'un dans le conteneur ", conteneur);
		return new moduleArbre(conteneur);
	}

	/**
	 * Génère l'id d'un enfant de l'arbre, à partir de son id et de l'id de son parent
	 * 
	 * @param {*} idParent 
	 * @param {*} idEnfant 
	 */
	function genererIdEnfant(idParent, idEnfant) {
		return idParent + "/" + idEnfant;
	}

	/**
	 * Génère le label d'un enfant de l'arbre, à partir de son id et de l'id de son parent 
	 * 
	 * @param {*} idParent 
	 * @param {*} idEnfant 
	 */
	function genererLabelEnfant(idParent, idEnfant) {
		return idEnfant;
	}

	/**
	 * Génère le label d'un artefact à partir d'un objet artefact 
	 * 
	 * @param {*} artefact 
	 */
	function genererLabelDependence(artefact) {
		return artefact.id;
	}

	return {
		creerArbre: creerArbre,
		genererLabelEnfant: genererLabelEnfant,
		genererIdEnfant: genererIdEnfant,
		genererLabelDependence: genererLabelDependence
	}
});
