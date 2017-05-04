// @flow
import { Component } from 'react';
import { findDOMNode } from 'react-dom';
import Box from './Box';
import Timeline from './Timeline';

import type { BoardView } from '../state';

type Props = {
  board: BoardView
};

class Graph extends Component {
  props: Props;

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
                    <section className="row" key={row.title}>
                      <h2>{row.title}</h2>
                      {
                        row.boxes.map(box => (
                          <Box box={box} key={box.title} ref={box.title} />
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
    if(lines && board) {
      document.title = board.title;
      lines.innerHTML = board.links
        .map(([from, to]) => {
          return [findDOMNode(this.refs[from]), findDOMNode(this.refs[to])];
        })
        .filter(([to, from]) => to && from)
        .reduce((html, [to, from]) => {
          return html + `
            <g>
              <path d="${spline(from, to)}" class="bgLine" />
              <path d="${spline(from, to)}" class="fgLine" />
            </g>
          `;
        }, '');
    }
  }
}

export default Graph;

const getBox = (el: HTMLElement): { x: number, y: number } => {
  return {
    x: el.getBoundingClientRect().left + (el.getBoundingClientRect().right - el.getBoundingClientRect().left) / 2,
    y: el.getBoundingClientRect().top + (el.getBoundingClientRect().bottom - el.getBoundingClientRect().top) / 2
  };
};

const spline = (from: HTMLElement, to: HTMLElement): string => {
  let fromBox = getBox(from), toBox = getBox(to);
  let a = fromBox.x < toBox.x ? fromBox : toBox, b = fromBox.x < toBox.x ? toBox : fromBox;
  let n = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)) * .75;
  return `M${a.x},${a.y} C${a.x + n},${a.y} ${b.x - n},${b.y} ${b.x},${b.y}`;
};