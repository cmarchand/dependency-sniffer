"use strict";
/**
 * Module de récupération des dépendances 
 * 
 */
define(function () {
	var host = "http://lx0115:8984";

	/**
	 * Construction de l'URL d'appel à l'API de recherche des enfants 
	 * 
	 * @param {*} parent 
	 */
	function construireURLEnfants(parent) {
		var url = host + "/deps/browse/" + parent + "/";
		//console.log("recherche des enfants " + url);

		return url;
	}

  	/**
	 * Construction de l'URL d'appel à l'API de recherche des dépendances 
	 * 
	 * @param {*} parent 
	 */
	function construireURLDependances(artefactId) {
		var url = host + "/deps/artifact/" + artefactId;

		return url;
	}

	/**
	 * Construction de l'URL d'appel à l'API de recherche des dépendances inversées
	 * 
	 * @param {*} parent 
	 */
	function construireURLDependancesInversees(artefactId) {
		var url = host + "/deps/reverse/" + artefactId;

		return url;
	}

	/**
	 * Recherche les enfants d'un package à l'aide de l'API et 
	 * appelle le callback d'alimentation de l'arbre
	 * 
	 * 
	 * @param {*} parent : le parant dont on cherche les enfants
	 * @param {*} callback : le callback d'alimentation de l'arbre des packages
	 */
	function chercherEnfants(parent, callback) {

		function chercherEnfantsCallback(response) {
			var enfants = [], i;

			for (i = 0; i < response.length; i++) {
				enfants.push(response[i]);
			}

			callback(enfants);
		}

		try {
			request("GET", construireURLEnfants(parent), chercherEnfantsCallback);
		} catch (err) {
			console.error("Erreur lors de la récupération des dépendances : " + err.message);
		}
	}

	/**
	 * Recherche le détail d'un artefact à l'aide de l'API puis appelle les callback 
	 * d'alimentation des arbres de dépendances
	 * 
	 * @param {*} artefactId : l'id de l'artefact dont on cherche le détail
	 * @param {*} callbackDependences : le callback qui va alimenter l'arbre des dépendances
	 * @param {*} callbackDependencesInversees : le callback qui va alimenter l'arbre des dépendances inversées
	 */
	function chercherDetail(artefactId, callbackDependences, callbackDependencesInversees) {
		artefactId = artefactId.replace(/\//g, ".");

		try {
			request("GET", construireURLDependances(artefactId), callbackDependences);
		} catch (err) {
			console.error("Erreur lors de la récupération des dépendances : " + err.message);
		}

		try {
			request("GET", construireURLDependancesInversees(artefactId), callbackDependencesInversees);
		} catch (err) {
			console.error("Erreur lors de la récupération des dépendances inversées : " + err.message);
		}

	}

	/**
	 * Requête une URL / Méthode à l'aide d'Ajax puis execute le callback passé en paramètre
	 * @param {*} httpMethode : le méthode HTTP
	 * @param {*} url : l'URL à appeler
	 * @param {*} callback : le callback à exécuter
	 */
	function request(httpMethode, url, callback) {
		var req = new XMLHttpRequest(), response;

		req.onreadystatechange = function (event) {
			var i, responseArray;
			// XMLHttpRequest.DONE === 4
			if (this.readyState === XMLHttpRequest.DONE) {
				if (this.status === 200) {
					//console.log("Réponse reçue: %s", this.responseText);

					response = JSON.parse(this.responseText);

					callback(response);
				} else {
					throw this.status + "(" + this.statusText + ")";
				}
			}
		};

		req.open(httpMethode, url, true);
		req.send(null);
	}
	
	/* Les bouchons : simuler les appels serveurs et renvoyer des données bidons */
	var i = 1;
	
	function chercherEnfantsBouchon(parent, callback) {
		i++;

		var enfants = [];

		if (i%2===0) {
			enfants.push({ name: "plop" });
			enfants.push({ name: "plAp" });
			enfants.push({ name: "plep", id: "plip" });
		} else {
			enfants.push({ name: "plep", id: "plip" });
			enfants.push({ name: "plop" });
		}
	//	enfants.push({ name: "plep", id: "plip" });
		//enfants.push({ name: "plAp" });
		
		callback(enfants);
	}

	function chercherDetailBouchon(artefactId, callback) {
		var donnees = {
			id: "fr.ggeorges.truc",
			designation: {
				artifactId: "plop",
				groupId: "fr.ggeorges",
				version: 12
			},
			dependencies: [
				{
					id: "fr.ggeorges.truc.machin.bidule",
					artifactId: "plAAp",
					groupId: "fr.ggeorges.bidule",
					version: 5
				},
				{
					id: "fr.ggeorges.truc",
					artifactId: "plUUp",
					groupId: "fr.ggeorges.truc",
					version: 15
				},
			],
			dependenciesInv: [
				{
					id: "UnTrucQuiEnDepend",
					artifactId: "plAAp",
					groupId: "fr.ggeorges.bidule",
					version: 5
				},
				{
					id: "UnTrucQuiEnDepend222",
					artifactId: "plUUp",
					groupId: "fr.ggeorges.truc",
					version: 15
				},
			]
		};

		callback(donnees);
	}	

	return {
		chercherEnfants: chercherEnfants,
		chercherDetail: chercherDetail,
	}
});
