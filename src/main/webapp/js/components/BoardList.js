// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { State, BoardConfig } from '../state';
import { fetchBoards } from '../actions';

type Props = {
  boards: Array<BoardConfig>,
  fetchBoards: () => void
};

const style = {
  boardList: {
    padding: '2em',
    overflow: 'auto'
  },
  boardLink: {
    textDecoration: 'none',
    fontSize: '3em',
    color: 'white',
    boxShadow: '0 0 24px rgba(0,0,0,0.5)',
    textAlign: 'center',
    padding: '.5em',
    background: '#2980b9',
    marginBottom: '.5em'
  }
};

class BoardList extends Component {
  props: Props;

  componentWillMount() {
    this.props.fetchBoards();
  }

  render() {
    const { boards } = this.props;
    return (
      <div className="board-list" style={ style.boardList }>
        {
          boards.map(board =>
            <a style={ style.boardLink } href={`/${board.title}`} key={board.title}>{board.title}</a>
          )
        }
      </div>
    );
  }
}

const select = (state: State) => ({
  boards: state.boards
});

const actions = dispatch => ({
  fetchBoards: () => dispatch(fetchBoards())
});

export default connect(select, actions)(BoardList);