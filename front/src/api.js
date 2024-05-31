import axios from 'axios';

const API_URL = 'http://localhost:8080/api/game';

export const getMoves = async () => {
    try {
        const response = await axios.get(`${API_URL}/moves`);
        return response.data;
    } catch (error) {
        console.error("Error fetching moves!", error);
        throw error;
    }
};

export const makeMove = async (move) => {
    try {
        await axios.post(`${API_URL}/move`, move);
        return response.data;
    } catch (error) {
        console.error("Error making move");
        throw error;
    }
};
