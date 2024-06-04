import axios from 'axios';

// TODO 
// This is a temp url to the /game/sp (singleplayer) API
// We need a way to separate between multiplayer games 
// and games against the engine
const API_URL = 'http://localhost:8080/api/game/sp';

export const getMoves = async () => {
    try {
        const response = await axios.get(`${API_URL}/moves`);
        return response.data;
    } catch (error) {
        console.error("Error fetching moves!", error);
        throw error;
    }
};

export const makeMove = async (move, singleplayer) => {
    try {
        const response = await axios.post(`${API_URL}/move`, move);
        return response.data;
    } catch (error) {
        console.error("Error making move", error);
        throw error;
    }
};

export const paul = async () => {
    try {
        const response = await axios.post(`${API_URL}/paul`);
    } catch (error) {
        console.error("Error: ", error);
        throw error;
    }
}
