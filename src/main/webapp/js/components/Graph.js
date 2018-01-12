// @flow
import { Component } from 'react';
import { findDOMNode } from 'react-dom';
import Box from './Box';
import Timeline from './Timeline';

import type { BoardView } from '../state';

type Props = {
  board: BoardView
};

type State = {
  hoveredBoxTitle: string
};

class Graph extends Component {
  props: Props;
  state: State;
  constructor(props: Props) {
    super(props);
    this.state = {
      hoveredBoxTitle: ''
    };
  }

  render() {
    const { board } = this.props;
    return (
      <div className='graph'>
        <header className={`${board.status} background`}>
          <h1>{board.title}</h1>
          <p>{board.message}</p>
        </header>
        <main>
          <svg ref="lines"></svg>
          {
            board.columns.map((col, i) => (
              <section className="column" key={i} style={ { 'flexBasis': `${col.percentage}%` } }>
                {
                  col.rows.map(row => (
                    <section className="row" key={row.title} style={ { 'flexBasis': `${row.percentage}%` } }>
                      <h2>{row.title}</h2>
                      {
                        row.boxes.map(box => (
                          <Box
                            box={box}
                            key={box.title}
                            ref={box.title}
                            onMouseEnter={() => this.setState({ hoveredBoxTitle: box.title })}
                            onMouseLeave={() => this.setState({ hoveredBoxTitle: '' })}
                          />
                        ))
                      }
                    </section>
                  ))
                }
              </section>
            ))
          }
        </main>
        <footer>
          <Timeline
            boardTitle={board.title}
          />
        </footer>
      </div>
    );
  }

  componentDidMount() {
    this.drawLines();
    window.addEventListener('resize', this.drawLines);
  }

  componentDidUpdate() {
    this.drawLines();
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.drawLines);
  }

  drawLines = () => {
    let lines = findDOMNode(this.refs.lines);
    const { board } = this.props;
    let hoveredTitle = this.state.hoveredBoxTitle;
    if(lines && board) {
      document.title = board.title;
      lines.innerHTML = board.links
        .map(([from, to]) => {
          return [from, to, findDOMNode(this.refs[from]), findDOMNode(this.refs[to])];
        })
        .filter(([_from, _to, fromNode, toNode]) => toNode && fromNode)
        .reduce((html, [from, to, fromNode, toNode]) => {
          let extraClass = '';
          if (hoveredTitle === from || hoveredTitle === to) {
            extraClass = ' hoveredPath';
          }
          return html + `
            <g>
              <path d="${spline(fromNode, toNode)}" class="bgLine${extraClass}" />
              <path d="${spline(fromNode, toNode)}" class="fgLine${extraClass}" />
            </g>
          `;
        }, '');
    }
  }
}

export default Graph;

const getBox = (el: HTMLElement): { x: number, y: number } => {
  let rect = el.getBoundingClientRect();
  return {
    x: rect.left + (rect.right - rect.left) / 2,
    y: rect.top + (rect.bottom - rect.top) / 2
  };
};

const spline = (from: HTMLElement, to: HTMLElement): string => {
  let fromBox = getBox(from), toBox = getBox(to);
  let a = fromBox.x < toBox.x ? fromBox : toBox, b = fromBox.x < toBox.x ? toBox : fromBox;
  let n = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)) * .75;
  return `M${a.x},${a.y} C${a.x + n},${a.y} ${b.x - n},${b.y} ${b.x},${b.y}`;
};
