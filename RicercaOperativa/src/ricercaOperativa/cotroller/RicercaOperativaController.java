package ricercaOperativa.cotroller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ricercaOperativa.Views.RicercaOperativaPanel;
import ricercaOperativa.model.Mezzo;
import ricercaOperativa.model.Nodo;
import ricercaOperativa.model.Tappa;

public class RicercaOperativaController {

	// private RicercaOperativaPanel view;
	// private JFrame frame;
	private double fuelTankCapacity;
	private double veichleLoadCapacity;
	private double fuelConsumptionRate;
	// private double maxDistanceForVeichle;
	private double inverseRefuelingRate;
	private double averageVelocity;
	private ArrayList<Mezzo> soluzioneMezzi;
	HashMap<Nodo, Double> distanzeClienti = new HashMap<Nodo, Double>();
	Tappa ultimaTappa;
	Double funzioneObbiettivo = 0.0;

	public RicercaOperativaController(final RicercaOperativaPanel view, JFrame frame) {
		// this.view = view;
		// this.frame = frame;
		int count = 0;
		ArrayList<Nodo> depositi = new ArrayList<Nodo>();
		ArrayList<Nodo> clienti = new ArrayList<Nodo>();
		ArrayList<Nodo> distributori = new ArrayList<Nodo>();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.showOpenDialog(frame);
		File file = fileChooser.getSelectedFile();
		if (file != null) {
			try {
				// create a Buffered Reader object instance with a FileReader
				BufferedReader br = new BufferedReader(new FileReader(file));

				// read the first line from the text file
				String fileRead = br.readLine();
				fileRead = br.readLine();

				// loop until all lines are read
				while (fileRead != null) {
					if (!fileRead.isEmpty()) {
						// use string.split to load a string array with the values from each line of
						// the file, using a comma as the delimiter
						String[] tokenize = fileRead.split("/");
						String id = tokenize[0];
						String type = tokenize[1];
						double x = Double.parseDouble((tokenize[2]));
						double y = Double.parseDouble(tokenize[3]);
						double demand = Double.parseDouble(tokenize[4]);
						double readyTime = Double.parseDouble(tokenize[5]);
						double dueDate = Double.parseDouble(tokenize[6]);
						double serviceTime = Double.parseDouble(tokenize[7]);
						Nodo nodo = new Nodo(id, type, x, y, demand, readyTime, dueDate, serviceTime);
						if (type.equals("f")) {
							distributori.add(nodo);
						} else if (type.equals("c")) {
							clienti.add(nodo);
						} else {
							depositi.add(nodo);
						}

						fileRead = br.readLine();
					} else {
						fileRead = br.readLine();
						while (fileRead != null) {
							String[] tokenize = fileRead.split("/");
							switch (count) {
							case 0:
								fuelTankCapacity = Double.parseDouble(tokenize[1]);
								count++;
								break;
							case 1:
								veichleLoadCapacity = Double.parseDouble(tokenize[1]);
								count++;
								break;
							case 2:
								fuelConsumptionRate = Double.parseDouble(tokenize[1]);
								count++;
								break;
							case 3:
								inverseRefuelingRate = Double.parseDouble(tokenize[1]);
								count++;
								break;
							case 4:
								averageVelocity = Double.parseDouble(tokenize[1]);
								count++;
								break;
							}
							fileRead = br.readLine();
						}
					}

				}
				// close file stream
				br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		trovaSoluzione(clienti, distributori, depositi);
	}

	public void trovaSoluzione(ArrayList<Nodo> clienti, ArrayList<Nodo> distributori, ArrayList<Nodo> depositi) {
		ArrayList<Mezzo> mezzi = new ArrayList<Mezzo>();
		boolean fullcapacity = false;
		while (!clienti.isEmpty()) {
			Tappa tappa = new Tappa(depositi.get(0), 0, 0);
			Mezzo mezzo = new Mezzo(fuelTankCapacity, veichleLoadCapacity, tappa);
			ultimaTappa = mezzo.getTappe().get(mezzo.getTappe().size() - 1);
			// se impostato a true vuol significare che non è possibile servire più nessun'altro cliente per motivi di tempo
			boolean timeException = false;
			while (fullcapacity == false && timeException == false && !clienti.isEmpty()) {
				// calcolo le distanze dei vari clienti dall'ultima tappa
				calcolaDistanze(clienti);
				// ordino il set di clienti in base alla loro distanza crescente
				distanzeClienti = sortHashMapByValues(distanzeClienti);
				Set<Nodo> clientiDaVisitare = distanzeClienti.keySet();
				for (Nodo nodo : clientiDaVisitare) {
					double arrivalTime = distanzeClienti.get(nodo) * averageVelocity + ultimaTappa.getDepartureTime();
					if (arrivalTime < nodo.getDueDate()) {
						if (mezzo.getLivelloCarico() >= nodo.getDemand()) {
							double carburanteNecessario = distanzeClienti.get(nodo) * fuelConsumptionRate;
							fullcapacity = false;
							if (mezzo.getLivelloCarburante() > carburanteNecessario) {
								double departureTime;
								if (arrivalTime >= nodo.getReadyTime()) {
									departureTime = arrivalTime + nodo.getServiceTime();
								} else {
									departureTime = arrivalTime + (nodo.getReadyTime() - arrivalTime) + nodo.getServiceTime();
								}
								Tappa prossimaTappa = new Tappa(nodo, arrivalTime, departureTime);
								mezzo.getTappe().add(prossimaTappa);
								mezzo.setLivelloCarburante(mezzo.getLivelloCarburante() - carburanteNecessario);
								mezzo.setLivelloCarico(mezzo.getLivelloCarico() - nodo.getDemand());
								ultimaTappa = prossimaTappa;
								clienti.remove(nodo);
								timeException = false;
							} else {
								timeException = false;
								aggiungiDistributore(clienti, mezzo, distributori);
							}
						} else {
							fullcapacity = true;
						}
					} else {
						timeException = true;
					}
				}
			}
			boolean fineTurno = false;
			while (fineTurno == false) {
				// devo tornare al deposito
				double distanzaDeposito = calcolaDistanza(ultimaTappa.getNodoDaVisitare(), depositi.get(0));
				double arrivalTimeInDepot = ultimaTappa.getDepartureTime() + (distanzaDeposito * averageVelocity);
				if (arrivalTimeInDepot < depositi.get(0).getDueDate()) {
					double carburante = calcolaDistanza(ultimaTappa.getNodoDaVisitare(), depositi.get(0)) * fuelConsumptionRate;
					if (mezzo.getLivelloCarburante() > carburante) {
						Tappa tappaFinale = new Tappa(depositi.get(0), arrivalTimeInDepot, 0);
						mezzo.getTappe().add(tappaFinale);
						mezzo.setLivelloCarburante(mezzo.getLivelloCarburante() - carburante);
						fineTurno = true;
					} else {
						// se non mi basta il carburante devo fare rifornimento
						aggiungiDistributore(clienti, mezzo, distributori);
					}
				} else {
					// se arrivo troppo tardi devo rimuovere l'ultimo cliente
					Tappa tappaDaRimuovore = mezzo.getTappe().get(mezzo.getTappe().size() - 1);
					clienti.add(tappaDaRimuovore.getNodoDaVisitare());
					mezzo.getTappe().remove(tappaDaRimuovore);
					ultimaTappa = mezzo.getTappe().get(mezzo.getTappe().size() - 1);
					mezzo.setLivelloCarburante((calcolaDistanza(tappaDaRimuovore.getNodoDaVisitare(), ultimaTappa.getNodoDaVisitare()) * fuelConsumptionRate) + mezzo.getLivelloCarburante());
					if (tappaDaRimuovore.getNodoDaVisitare().getType().equals("c")) {
						mezzo.setLivelloCarico(mezzo.getLivelloCarico() + tappaDaRimuovore.getNodoDaVisitare().getDemand());
					}
				}
			}
			mezzi.add(mezzo);
		}
		funzioneObbiettivo = Double.valueOf(mezzi.size());
		for (Mezzo mezzo : mezzi) {
			calcolaDistanzaPercorsa(mezzo);
			funzioneObbiettivo = funzioneObbiettivo + (mezzo.getKmInEccedenza() * 0.5);
		}
		soluzioneMezzi = mezzi;
		simulatedAnnealing(mezzi);
		// writeSolution(mezzi);
	}

	public double calcolaTempo(Nodo nodo1, Nodo nodo2) {
		double distance = calcolaDistanza(nodo1, nodo2);
		double timeDuration = distance / averageVelocity;
		return timeDuration;
	}

	public double calcolaDistanza(Nodo nodo1, Nodo nodo2) {
		double distance = Math.sqrt(Math.pow(nodo2.getX() - nodo1.getX(), 2) + Math.pow(nodo2.getY() - nodo1.getY(), 2));
		return distance;
	}

	public double costoDistributore(Nodo distributore, Nodo nodo1, Nodo nodo2) {
		double costo = calcolaDistanza(distributore, nodo2) + calcolaDistanza(nodo1, distributore) - calcolaDistanza(nodo1, nodo2);
		return costo;
	}

	public void aggiungiDistributore(ArrayList<Nodo> clienti, Mezzo mezzo, ArrayList<Nodo> distributori) {

		// se non ho carburante necessario per raggiungere il cliente più vicino vado al distributore più vicino
		// calcolo le distanze dei vari clienti
		HashMap<Nodo, Double> distanzeDistributori = new HashMap<Nodo, Double>();

		for (Nodo distributore : distributori) {
			double distanza = calcolaDistanza(ultimaTappa.getNodoDaVisitare(), distributore);
			distanzeDistributori.put(distributore, Double.valueOf(distanza));
		}
		// ordino il set di distributori in base alla loro distanza crescente
		distanzeDistributori = sortHashMapByValues(distanzeDistributori);
		Set<Nodo> DistributoriDaVisitare = distanzeDistributori.keySet();
		for (Nodo nodoDistributore : DistributoriDaVisitare) {
			double arrivalTimeDistributore = distanzeDistributori.get(nodoDistributore) * averageVelocity + ultimaTappa.getDepartureTime();
			if (arrivalTimeDistributore < nodoDistributore.getDueDate()) {
				double carburanteNecc = distanzeDistributori.get(nodoDistributore) * fuelConsumptionRate;
				if (mezzo.getLivelloCarburante() > carburanteNecc) {
					double tempoRifornimento = (fuelTankCapacity - mezzo.getLivelloCarburante() + carburanteNecc) * inverseRefuelingRate;
					double departureTime = arrivalTimeDistributore + tempoRifornimento;
					Tappa prossimaTappa = new Tappa(nodoDistributore, arrivalTimeDistributore, departureTime);
					double carburanteRestante = fuelTankCapacity;
					mezzo.getTappe().add(prossimaTappa);
					mezzo.setLivelloCarburante(carburanteRestante);
					ultimaTappa = prossimaTappa;
					break;
				} else {
					double kmInPiu = (carburanteNecc - mezzo.getLivelloCarburante()) * fuelConsumptionRate;
					mezzo.setKmInEccedenza(mezzo.getKmInEccedenza() + kmInPiu);
					double tempoRifornimento = fuelTankCapacity * inverseRefuelingRate;
					double departureTime = arrivalTimeDistributore + tempoRifornimento;
					Tappa prossimaTappa = new Tappa(nodoDistributore, arrivalTimeDistributore, departureTime);
					double carburanteRestante = fuelTankCapacity;
					mezzo.getTappe().add(prossimaTappa);
					mezzo.setLivelloCarburante(carburanteRestante);
					ultimaTappa = prossimaTappa;
					break;
				}

			}
		}
		calcolaDistanze(clienti);
	}

	public LinkedHashMap<Nodo, Double> sortHashMapByValues(HashMap<Nodo, Double> passedMap) {
		List<Nodo> mapKeys = new ArrayList<Nodo>(passedMap.keySet());
		List<Double> mapValues = new ArrayList<Double>(passedMap.values());
		Collections.sort(mapValues);

		LinkedHashMap<Nodo, Double> sortedMap = new LinkedHashMap<Nodo, Double>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = valueIt.next();
			Iterator<Nodo> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Nodo key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public void calcolaDistanze(ArrayList<Nodo> clienti) {
		distanzeClienti.clear();
		for (Nodo clienteDaVisitare : clienti) {
			double distanza = calcolaDistanza(ultimaTappa.getNodoDaVisitare(), clienteDaVisitare);
			distanzeClienti.put(clienteDaVisitare, Double.valueOf(distanza));

		}
	}

	// public void writeSolution(ArrayList<Mezzo> mezzi) {
	// try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/NicoMac/Desktop/prova.txt"), "utf-8"))) {
	// int i = 1;
	// writer.write("N.Corsa \t Nodo \t ArrivalTime \t DeparturTime \n ");
	// for (Mezzo mezzo : mezzi) {
	// for (Tappa tappa : mezzo.getTappe()) {
	// writer.write(i + ") \t" +tappa.getNodoDaVisitare().getId()+"\t" + tappa.getArrivalTime() + "\t" + tappa.getDepartureTime() + "\n");
	// }
	// i++;
	// writer.write(mezzo.getLivelloCarburante() + "\t" + mezzo.getLivelloCarico() + "\t" + "Eccedenza km: " + mezzo.getKmInEccedenza() + "\n");
	// }
	//
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }

	public void calcolaDistanzaPercorsa(Mezzo mezzo) {
		double distanzaPercorsa = 0;
		int numeroTappe = mezzo.getTappe().size();
		for (int i = 0; i < numeroTappe - 1; i++) {
			distanzaPercorsa = distanzaPercorsa + (calcolaDistanza(mezzo.getTappe().get(i).getNodoDaVisitare(), mezzo.getTappe().get(i + 1).getNodoDaVisitare()));
		}
		mezzo.setKmPercorsi(distanzaPercorsa);
		System.out.println(String.valueOf(distanzaPercorsa));

	}

	public void simulatedAnnealing(ArrayList<Mezzo> mezzi) {
		double startingTemperature = 50;
		double temperature = startingTemperature;
		double finalTemperature = 0;
		double temperatureOfBestSolution;
		double temperatureOfReset = 50;
		ArrayList<Mezzo> currentSolution = mezzi;
		double valueOfCurrentSolution = funzioneObbiettivo;
		ArrayList<Mezzo> bestSolution = mezzi;
		double valueOfBestSolution = funzioneObbiettivo;
		int numberOfReset = 3;
		double decrementConstant = 0.5;
		int numeroMezzi = mezzi.size();
		for (int i = 0; i < mezzi.size() - 1; i++) {
			int startCheckFrom = i + 1;
			Mezzo mezzo1 = mezzi.get(i);
			Mezzo mezzo2 = mezzi.get(startCheckFrom);
			// scambio (0,1), ma la tappa che prendo non può essere il deposito!
			for (Tappa tappaDaControllare : mezzo2.getTappe()) {
				if (!tappaDaControllare.getNodoDaVisitare().getType().equals("d")) {
					Nodo nodoDaRimuovere = tappaDaControllare.getNodoDaVisitare();
					mezzo2.getTappe().remove(tappaDaControllare);
					boolean mezzo2Valido = aggiornaPercorsoMezzo(mezzo2);
					if (mezzo2Valido) {
						int count = 0;
						for (Tappa tappa : mezzo1.getTappe()) {
							if (tappa.getNodoDaVisitare().getReadyTime() > nodoDaRimuovere.getReadyTime()) {
								Tappa nuovaTappa = new Tappa(nodoDaRimuovere, 0, 0);
								mezzo1.getTappe().add(count, nuovaTappa);
								boolean mezzo1Valido = aggiornaPercorsoMezzo(mezzo1);
								if (mezzo1Valido) {
									double nuvoCostoFunzione = mezzi.size();
									for (Mezzo mezzo : mezzi) {
										nuvoCostoFunzione = nuvoCostoFunzione + (mezzo.getKmInEccedenza() * 0.5);
									}
									double delta = nuvoCostoFunzione - funzioneObbiettivo;
									if (delta <= 0 || (delta > 0 && Math.pow(Math.E, (-delta / temperature)) >= Math.random())) {
										mezzi.get(i).setMezzo(mezzo1);
										mezzi.get(startCheckFrom).setMezzo(mezzo2);
										currentSolution = mezzi;
										valueOfCurrentSolution = nuvoCostoFunzione;
										if (valueOfCurrentSolution < valueOfBestSolution) {
											bestSolution = mezzi;
											valueOfBestSolution = valueOfCurrentSolution;
											temperatureOfBestSolution = temperature;
											simulatedAnnealing(mezzi);
										} else {

										}
									}
								}
							}
							count++;
						}
					}
				}
			}
		}
	}

	public boolean aggiornaPercorsoMezzo(Mezzo mezzo) {

		boolean valido = true;
		mezzo.setLivelloCarburante(fuelTankCapacity);
		mezzo.setLivelloCarico(veichleLoadCapacity);
		for (int i = 0; i < mezzo.getTappe().size() - 1; i++) {
			Nodo nodoDaAggiornare = mezzo.getTappe().get(i + 1).getNodoDaVisitare();
			double distanza = calcolaDistanza(mezzo.getTappe().get(i).getNodoDaVisitare(), nodoDaAggiornare);
			double livelloCarburante = mezzo.getLivelloCarburante() - (distanza * fuelConsumptionRate);
			double arrivalTime = mezzo.getTappe().get(i).getDepartureTime() + (distanza + averageVelocity);
			double departureTime = 0;
			double livelloCarico = mezzo.getLivelloCarico() - nodoDaAggiornare.getDemand();
			if (nodoDaAggiornare.getType().equals("f")) {
				if (arrivalTime < nodoDaAggiornare.getDueDate()) {
					if (livelloCarburante < 0) {
						departureTime = arrivalTime + (fuelTankCapacity * inverseRefuelingRate);
						double kmInEccedenza = (-livelloCarburante) * fuelConsumptionRate;
						mezzo.setKmInEccedenza(kmInEccedenza);
						mezzo.getTappe().get(i + 1).setArrivalTime(arrivalTime);
						mezzo.getTappe().get(i + 1).setDepartureTime(departureTime);
						mezzo.setLivelloCarburante(fuelTankCapacity);
					} else {
						double rifornimentoNecessario = fuelTankCapacity - livelloCarburante;
						departureTime = arrivalTime + (rifornimentoNecessario * inverseRefuelingRate);
						mezzo.getTappe().get(i + 1).setArrivalTime(arrivalTime);
						mezzo.getTappe().get(i + 1).setDepartureTime(departureTime);
						mezzo.setLivelloCarburante(fuelTankCapacity);
					}
				} else {
					valido = false;
					break;
				}
			} else {
				// carico veicolo
				if (arrivalTime < nodoDaAggiornare.getDueDate()) {
					if (livelloCarburante > 0) {
						if (livelloCarico >= 0) {
							if (arrivalTime > nodoDaAggiornare.getReadyTime()) {
								departureTime = arrivalTime + nodoDaAggiornare.getServiceTime();
								mezzo.getTappe().get(i + 1).setArrivalTime(arrivalTime);
								mezzo.getTappe().get(i + 1).setDepartureTime(departureTime);
								mezzo.setLivelloCarburante(livelloCarburante);
							} else {
								departureTime = arrivalTime + (nodoDaAggiornare.getReadyTime() - arrivalTime) + nodoDaAggiornare.getServiceTime();
								mezzo.getTappe().get(i + 1).setArrivalTime(arrivalTime);
								mezzo.getTappe().get(i + 1).setDepartureTime(departureTime);
								mezzo.setLivelloCarburante(livelloCarburante);
							}
						} else {
							valido = false;
							break;
						}
					} else {
						valido = false;
						break;
					}
				} else {
					valido = false;
					break;
				}
			}
		}
		return valido;
	}
}
